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

import java.util.function.Predicate;

/**
 * A composable constraint on a stream.
 */
public interface StreamSpec<T> {

    boolean isSatisfiedBy(T stream);

    default StreamSpec<T> and(StreamSpec<T> other) {
        return stream -> isSatisfiedBy(stream) && other.isSatisfiedBy(stream);
    }

    default StreamSpec<T> not() {
        return stream -> !isSatisfiedBy(stream);
    }

    default Predicate<T> asPredicate() {
        return this::isSatisfiedBy;
    }
}
