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

/**
 * Prefers the highest resolution, then the best container, then the highest audio bitrate.
 */
public final class BestQualityRanking implements StreamRanking {

    @Override
    public Comparator<VideoStream> videoOrder() {
        Comparator<VideoStream> byResolution = Comparator.comparing(Resolutions::of, Comparator.reverseOrder());
        return byResolution.thenComparing(VideoStream::getFormat, FormatPreference.INSTANCE);
    }

    @Override
    public Comparator<AudioStream> audioOrder() {
        return Comparator.comparingInt(AudioStream::getAverageBitrate).reversed();
    }
}
