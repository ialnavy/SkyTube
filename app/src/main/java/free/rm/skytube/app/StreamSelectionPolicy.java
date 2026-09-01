/*
 * SkyTube
 * Copyright (C) 2020  Zsombor Gegesy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package free.rm.skytube.app;

import android.content.Context;
import android.net.Uri;

import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.ArrayList;
import java.util.List;

import free.rm.skytube.R;
import free.rm.skytube.BuildConfig;
import free.rm.skytube.app.stream.Resolutions;
import free.rm.skytube.app.stream.rank.FormatPreference;
import free.rm.skytube.app.stream.rank.StreamRanking;
import free.rm.skytube.app.stream.spec.AudioTrackIs;
import free.rm.skytube.app.stream.spec.DirectUrl;
import free.rm.skytube.app.stream.spec.FormatIn;
import free.rm.skytube.app.stream.spec.ResolutionWithin;
import free.rm.skytube.app.stream.spec.StreamSpec;
import free.rm.skytube.businessobjects.Logger;
import free.rm.skytube.businessobjects.YouTube.VideoStream.VideoQuality;
import free.rm.skytube.businessobjects.YouTube.VideoStream.VideoResolution;

public class StreamSelectionPolicy {

    private final boolean allowVideoOnly;
    private final VideoResolution maxResolution;
    private final VideoResolution minResolution;
    private final VideoQuality videoQuality;
    private final StreamRanking ranking;
    private final StreamSpec<VideoStream> videoSpec;
    private final StreamSpec<AudioStream> audioSpec;

    public StreamSelectionPolicy(boolean allowVideoOnly, VideoResolution maxResolution, VideoResolution minResolution, VideoQuality videoQuality) {
        this.allowVideoOnly = allowVideoOnly;
        this.maxResolution = maxResolution != VideoResolution.RES_UNKNOWN ? maxResolution : null;
        this.minResolution = minResolution != VideoResolution.RES_UNKNOWN ? minResolution : null;
        this.videoQuality = videoQuality;
        this.ranking = StreamRanking.forQuality(videoQuality);
        this.videoSpec = new DirectUrl<VideoStream>()
                .and(new FormatIn<VideoStream>(FormatPreference.SUPPORTED_VIDEO_CONTAINERS))
                .and(new ResolutionWithin(this.minResolution, this.maxResolution));
        this.audioSpec = new AudioTrackIs(AudioTrackType.ORIGINAL);
    }

    public StreamSelectionPolicy withAllowVideoOnly(boolean newValue) {
        return new StreamSelectionPolicy(newValue, maxResolution, minResolution, videoQuality);
    }

    public StreamSelection select(StreamInfo streamInfo) {
        VideoStream videoStream = pickVideo(streamInfo);
        if (videoStream == null) {
            return null;
        }
        VideoResolution resolution = Resolutions.of(videoStream);
        if (!videoStream.isVideoOnly()) {
            return new StreamSelection(videoStream, resolution, null);
        }
        AudioStream audioStream = pickAudio(streamInfo);
        return audioStream != null ? new StreamSelection(videoStream, resolution, audioStream) : null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StreamSelectionPolicy{");
        sb.append("allowVideoOnly=").append(allowVideoOnly);
        if (maxResolution != null) {
            sb.append(", maxResolution=").append(maxResolution);
        }
        if (minResolution != null) {
            sb.append(", minResolution=").append(minResolution);
        }
        sb.append(", videoQuality=").append(videoQuality);
        sb.append('}');
        return sb.toString();
    }

    public String getErrorMessage(Context context) {
        String min = "*";
        String max = "*";
        if (minResolution != null) {
            min = minResolution.name();
        }
        if (maxResolution != null) {
            max = maxResolution.name();
        }
        return context.getString(R.string.video_stream_not_found_with_request_resolution, min, max);
    }

    private VideoStream pickVideo(StreamInfo streamInfo) {
        List<VideoStream> streams = new ArrayList<>(streamInfo.getVideoStreams());
        if (allowVideoOnly) {
            streams.addAll(streamInfo.getVideoOnlyStreams());
        }
        if (BuildConfig.DEBUG) {
            for (VideoStream stream : streams) {
                Logger.d(this, "found %s", toHumanReadable(stream));
            }
        }
        VideoStream best = streams.stream()
                .filter(videoSpec.asPredicate())
                .min(ranking.videoOrder())
                .orElse(null);
        if (BuildConfig.DEBUG) {
            Logger.d(this, "best -> %s", toHumanReadable(best));
        }
        return best;
    }

    private AudioStream pickAudio(StreamInfo streamInfo) {
        if (BuildConfig.DEBUG) {
            for (AudioStream stream : streamInfo.getAudioStreams()) {
                Logger.d(this, "AudioStream %s", toHumanReadable(stream));
            }
        }
        AudioStream best = streamInfo.getAudioStreams().stream()
                .filter(audioSpec.asPredicate())
                .min(ranking.audioOrder())
                .orElse(null);
        if (BuildConfig.DEBUG) {
            Logger.d(this, "best %s", toHumanReadable(best));
        }
        return best;
    }

    private static String toHumanReadable(AudioStream as) {
        return as != null ? "AudioStream(" + as.getAverageBitrate() + ", " + as.getFormat() + ", codec=" + as.getCodec() + ", q=" + as.getQuality() + ", isUrl=" + as.isUrl() + ",delivery=" + as.getDeliveryMethod() + ")" : "NULL";
    }

    private static String toHumanReadable(VideoStream vs) {
        return vs != null ? "VideoStream(" + Resolutions.of(vs).name() +
                ", format=" + vs.getFormat() +
                ", codec=" + vs.getCodec() +
                ", quality=" + vs.getQuality() +
                ",videoOnly=" + vs.isVideoOnly() +
                ",isUrl=" + vs.isUrl() +
                ",delivery=" + vs.getDeliveryMethod() + ")" : "NULL";
    }

    public static class StreamSelection {
        final VideoStream videoStream;
        final VideoResolution resolution;
        final AudioStream audioStream;

        StreamSelection(VideoStream videoStream, VideoResolution resolution, AudioStream audioStream) {
            this.videoStream = videoStream;
            this.resolution = resolution;
            this.audioStream = audioStream;
        }

        public VideoStream getVideoStream() {
            return videoStream;
        }

        public Uri getVideoStreamUri() {
            return videoStream.isUrl() ? Uri.parse(videoStream.getContent()) : null;
        }

        public Uri getAudioStreamUri() {
            return audioStream != null && audioStream.isUrl() ? Uri.parse(audioStream.getContent()) : null;
        }

        public VideoResolution getResolution() {
            return resolution;
        }

        public AudioStream getAudioStream() {
            return audioStream;
        }
    }
}
