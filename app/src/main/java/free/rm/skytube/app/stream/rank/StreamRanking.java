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

import free.rm.skytube.businessobjects.YouTube.VideoStream.VideoQuality;

/**
 * Strategy deciding which of two streams is preferable.
 */
public interface StreamRanking {

    /** Best-first ordering, so the preferred stream is the minimum. */
    Comparator<VideoStream> videoOrder();

    /** Best-first ordering, so the preferred stream is the minimum. */
    Comparator<AudioStream> audioOrder();

    static StreamRanking forQuality(VideoQuality videoQuality) {
        switch (videoQuality) {
            case BEST_QUALITY:
                return new BestQualityRanking();
            case LEAST_BANDWIDTH:
                return new LeastBandwidthRanking();
            default:
                throw new IllegalArgumentException("Unexpected videoQuality:" + videoQuality);
        }
    }
}
