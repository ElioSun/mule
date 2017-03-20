package org.mule.runtime.core.streaming.objects;

import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.api.streaming.objects.CursorIteratorProvider;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.functional.Either;

import java.util.Iterator;

public interface CursorIteratorProviderFactory {

  /**
   * Optionally creates a new {@link CursorIteratorProvider} to buffer the given {@code iterator}.
   * <p>
   * Implementations might resolve that the given iterator is/should not be buffered and thus
   * it will return the same given iterator. In that case, the iterator will be unaltered.
   *
   * @param event       the event on which buffering is talking place
   * @param iterator the stream to be buffered
   * @return {@link Either} a {@link CursorStreamProvider} or the same given {@code inputStream}
   */
  Either<CursorIteratorProvider, Iterator> of(Event event, Iterator iterator);
}
