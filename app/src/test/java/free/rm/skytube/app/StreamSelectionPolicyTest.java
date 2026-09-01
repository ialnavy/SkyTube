package free.rm.skytube.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.AudioTrackType;
import org.schabi.newpipe.extractor.stream.DeliveryMethod;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamType;
import org.schabi.newpipe.extractor.stream.VideoStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import free.rm.skytube.businessobjects.YouTube.VideoStream.VideoQuality;
import free.rm.skytube.businessobjects.YouTube.VideoStream.VideoResolution;

public class StreamSelectionPolicyTest {

    @Test
    void testBestVideoQualitySelection() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(false, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.BEST_QUALITY);
        test(policy, "1080P", "480P", "720P", "1080P", "1440P");
        test(policy, "1080P", "480P", "720P", "1080P");
        test(policy, "720P", "480P", "720P");
        test(policy, null, "144P", "1440P");
    }

    @Test
    void testLeastNetworkUsageSelection() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(false, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.LEAST_BANDWIDTH);
        test(policy, "480P", "144P", "480P", "720P", "1080P", "1440P");
        test(policy, "480P", "480P", "720P", "1080P");
        test(policy, "720P", "720P", "1080P");
        test(policy, null, "144P", "1440P");
    }

    @Test
    void testBetterFormatSelection() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(false, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.BEST_QUALITY);

        test(policy, MediaFormat.MPEG_4, MediaFormat.v3GPP, MediaFormat.MPEG_4);
        test(policy, MediaFormat.WEBM, MediaFormat.v3GPP, MediaFormat.WEBM, MediaFormat.MPEG_4);
        test(policy, MediaFormat.MPEG_4, MediaFormat.VTT, MediaFormat.MPEG_4);
    }

    @Test
    void testBetterFormatSelectionForLeastBandwith() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(false, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.LEAST_BANDWIDTH);

        test(policy, MediaFormat.MPEG_4, MediaFormat.v3GPP, MediaFormat.MPEG_4);
        test(policy, MediaFormat.WEBM, MediaFormat.v3GPP, MediaFormat.WEBM, MediaFormat.MPEG_4);
        test(policy, MediaFormat.MPEG_4, MediaFormat.VTT, MediaFormat.MPEG_4);
    }

    @Test
    void testVideoOnlyStreamsAreIgnoredWhenNotAllowed() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(false, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.BEST_QUALITY);

        StreamInfo streamInfo = createStreamInfo();
        streamInfo.setVideoOnlyStreams(createVideoStreams(true, "1080P"));
        streamInfo.setAudioStreams(Arrays.asList(createAudioStream(128, AudioTrackType.ORIGINAL)));

        Assertions.assertNull(policy.select(streamInfo), "Video only streams must not be used by a policy which forbids them");
    }

    @Test
    void testVideoOnlyStreamIsMergedWithTheBestOriginalAudio() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(true, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.BEST_QUALITY);

        StreamInfo streamInfo = createStreamInfo();
        streamInfo.setVideoStreams(createVideoStreams(false, "480P"));
        streamInfo.setVideoOnlyStreams(createVideoStreams(true, "1080P"));
        streamInfo.setAudioStreams(Arrays.asList(
                createAudioStream(128, AudioTrackType.ORIGINAL),
                createAudioStream(256, AudioTrackType.ORIGINAL)));

        StreamSelectionPolicy.StreamSelection selection = policy.select(streamInfo);
        Assertions.assertEquals("1080P", selection.getVideoStream().resolution);
        Assertions.assertEquals(256, selection.getAudioStream().getAverageBitrate());
    }

    @Test
    void testVideoOnlyStreamIsRejectedWithoutAnOriginalAudioTrack() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(true, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.BEST_QUALITY);

        StreamInfo streamInfo = createStreamInfo();
        streamInfo.setVideoOnlyStreams(createVideoStreams(true, "1080P"));
        streamInfo.setAudioStreams(Arrays.asList(createAudioStream(256, AudioTrackType.DUBBED)));

        Assertions.assertNull(policy.select(streamInfo), "A video only stream is unusable without an audio track");
    }

    @Test
    void testDubbedAudioTracksAreIgnoredAndUnknownOnesAccepted() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(true, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.BEST_QUALITY);

        StreamInfo streamInfo = createStreamInfo();
        streamInfo.setVideoOnlyStreams(createVideoStreams(true, "1080P"));
        streamInfo.setAudioStreams(Arrays.asList(
                createAudioStream(320, AudioTrackType.DUBBED),
                createAudioStream(192, null),
                createAudioStream(128, AudioTrackType.ORIGINAL)));

        StreamSelectionPolicy.StreamSelection selection = policy.select(streamInfo);
        Assertions.assertEquals(192, selection.getAudioStream().getAverageBitrate());
    }

    @Test
    void testLeastBandwidthPicksTheLowestAudioBitrate() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(true, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.LEAST_BANDWIDTH);

        StreamInfo streamInfo = createStreamInfo();
        streamInfo.setVideoOnlyStreams(createVideoStreams(true, "480P"));
        streamInfo.setAudioStreams(Arrays.asList(
                createAudioStream(256, AudioTrackType.ORIGINAL),
                createAudioStream(128, AudioTrackType.ORIGINAL)));

        StreamSelectionPolicy.StreamSelection selection = policy.select(streamInfo);
        Assertions.assertEquals(128, selection.getAudioStream().getAverageBitrate());
    }

    @Test
    void testStreamsWithoutADirectUrlAreIgnored() {
        StreamSelectionPolicy policy = new StreamSelectionPolicy(false, VideoResolution.RES_1080P, VideoResolution.RES_480P, VideoQuality.BEST_QUALITY);

        StreamInfo streamInfo = createStreamInfo();
        streamInfo.setVideoStreams(Arrays.asList(
                new VideoStream.Builder()
                        .setId("manifest-1080P")
                        .setContent("<manifest/>", false)
                        .setMediaFormat(MediaFormat.WEBM)
                        .setResolution("1080P")
                        .setIsVideoOnly(false)
                        .setDeliveryMethod(DeliveryMethod.DASH)
                        .build()));

        Assertions.assertNull(policy.select(streamInfo), "A stream whose content is a manifest cannot be played directly");
    }

    private void test(StreamSelectionPolicy policy, String expectedResolution, String... resolutions) {
        StreamInfo streamInfo = createStreams(resolutions);
        StreamSelectionPolicy.StreamSelection selection = policy.select(streamInfo);
        if (expectedResolution != null) {
            Assertions.assertEquals(expectedResolution, selection.getVideoStream().resolution, "Expecting " + policy + ", available resolutions: " + Arrays.asList(resolutions));
        } else {
            Assertions.assertNull(selection, "Nothing should match");
        }
    }

    private void test(StreamSelectionPolicy policy, MediaFormat expectedFormat, MediaFormat... mediaFormats) {
        StreamInfo streamInfo = createStreams(mediaFormats);
        StreamSelectionPolicy.StreamSelection selection = policy.select(streamInfo);
        if (expectedFormat != null) {
            Assertions.assertEquals(expectedFormat, selection.getVideoStream().getFormat(), "Expecting " + policy + ", available formats: " + Arrays.asList(mediaFormats));
        } else {
            Assertions.assertNull(selection, "Nothing should match");
        }
    }

    private StreamInfo createStreams(MediaFormat... mediaFormats) {
        List<VideoStream> streams = new ArrayList<>();
        for (MediaFormat mediaFormat : mediaFormats) {
            streams.add(
                new VideoStream.Builder()
                    .setId("id-"+mediaFormat.hashCode())
                        .setContent("url/" + mediaFormat, true)
                        .setMediaFormat(mediaFormat)
                        .setResolution("1080P")
                        .setDeliveryMethod(DeliveryMethod.PROGRESSIVE_HTTP)
                        .setIsVideoOnly(false)
                        .build());
        }
        return createStreams(streams);
    }

    private StreamInfo createStreams(String... resolutions) {
        return createStreams(createVideoStreams(false, resolutions));
    }

    private List<VideoStream> createVideoStreams(boolean videoOnly, String... resolutions) {
        List<VideoStream> streams = new ArrayList<>();
        for (String resolution : resolutions) {
            streams.add(
                new VideoStream.Builder()
                    .setId("id-" + resolution.hashCode() + (videoOnly ? "-videoonly" : ""))
                    .setContent("url/" + resolution, true)
                    .setMediaFormat(MediaFormat.WEBM)
                    .setResolution(resolution)
                    .setIsVideoOnly(videoOnly)
                    .setDeliveryMethod(DeliveryMethod.DASH)
                    .build());
        }
        return streams;
    }

    private AudioStream createAudioStream(int averageBitrate, AudioTrackType trackType) {
        return new AudioStream.Builder()
            .setId("audio-" + averageBitrate + "-" + trackType)
            .setContent("url/audio/" + averageBitrate, true)
            .setMediaFormat(MediaFormat.M4A)
            .setAverageBitrate(averageBitrate)
            .setAudioTrackType(trackType)
            .setDeliveryMethod(DeliveryMethod.DASH)
            .build();
    }

    private StreamInfo createStreams(List<VideoStream> streams) {
        StreamInfo si = createStreamInfo();
        si.setVideoStreams(streams);
        return si;
    }

    private StreamInfo createStreamInfo() {
        return new StreamInfo(0, "url", "originalUrl", StreamType.VIDEO_STREAM, "id", "name", -1);
    }
}
