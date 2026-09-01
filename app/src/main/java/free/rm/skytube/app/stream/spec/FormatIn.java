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

package free.rm.skytube.app.stream.spec;

import org.schabi.newpipe.extractor.MediaFormat;
import org.schabi.newpipe.extractor.stream.Stream;

import java.util.Collection;

/**
 * Accepts only streams packaged in one of the given containers.
 */
public final class FormatIn<T extends Stream> implements StreamSpec<T> {

    private final Collection<MediaFormat> allowedFormats;

    public FormatIn(Collection<MediaFormat> allowedFormats) {
        this.allowedFormats = allowedFormats;
    }

    @Override
    public boolean isSatisfiedBy(T stream) {
        return allowedFormats.contains(stream.getFormat());
    }
}
