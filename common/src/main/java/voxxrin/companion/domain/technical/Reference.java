package voxxrin.companion.domain.technical;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import voxxrin.companion.domain.Type;
import voxxrin.companion.domain.Type;
import voxxrin.companion.persistence.ReferenceResolver;
import voxxrin.companion.serialization.ReferenceDeserializer;
import voxxrin.companion.serialization.ReferenceSerializer;

@JsonSerialize(using = ReferenceSerializer.class)
@JsonDeserialize(using = ReferenceDeserializer.class)
public class Reference<T extends Referenceable> implements Supplier<T> {

    private ElementURI uri;

    private Optional<T> target;

    private static final ThreadLocal<ReferenceResolver> resolver = new ThreadLocal<>();

    private Reference(ElementURI uri) {
        this.uri = uri;
    }

    public static <T extends Referenceable> Reference<T> of(ElementURI uri) {
        return new Reference<>(uri);
    }

    public static <T extends Referenceable> Reference<T> of(Type type, String key) {
        return new Reference<>(ElementURI.of(type, key));
    }

    private T resolve() {
        if (resolver.get() == null) {
            return null;
        }
        return resolver.get().resolve(uri);
    }

    public static ThreadLocal<ReferenceResolver> getResolver() {
        return resolver;
    }

    @Override
    public T get() {
        if (target != null && target.isPresent()) {
            return target.get();
        }
        target = Optional.of(resolve());
        return target.get();
    }

    public Optional<T> maybeGet() {
        if (target != null && target.isPresent()) {
            return target;
        }
        return target = Optional.fromNullable(resolve());
    }

    public ElementURI getUri() {
        return uri;
    }
}
