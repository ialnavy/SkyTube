/*
 * SkyTube
 * Copyright (C) 2025  SkyTube Team
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

package free.rm.skytube.app.stream.rank;

import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.Comparator;

import free.rm.skytube.app.stream.Resolutions;
import free.rm.skytube.businessobjects.YouTube.VideoStream.VideoResolution;

/**
 * Prefers the lowest resolution, then the best container, then the lowest audio bitrate.
 */
public final class LeastBandwidthRanking implements StreamRanking {

    @Override
    public Comparator<VideoStream> videoOrder() {
        Comparator<VideoStream> byResolution = Comparator.comparingInt(LeastBandwidthRanking::costOf);
        return byResolution.thenComparing(VideoStream::getFormat, FormatPreference.INSTANCE);
    }

    @Override
    public Comparator<AudioStream> audioOrder() {
        return Comparator.comparingInt(AudioStream::getAverageBitrate);
    }

    /** An unparseable resolution ranks last: its bandwidth cost cannot be assumed to be the lowest. */
    private static int costOf(VideoStream stream) {
        VideoResolution resolution = Resolutions.of(stream);
        return resolution == VideoResolution.RES_UNKNOWN ? Integer.MAX_VALUE : resolution.ordinal();
    }
}
