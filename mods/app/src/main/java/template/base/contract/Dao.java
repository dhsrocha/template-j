package template.base.contract;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.impl.DSL;
import template.base.Body;
import template.base.Exceptions;

/**
 * Data Access Object, ensembles persistence communication concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Dao {

  /**
   * Loads an mapping handler for a given database mapping type.
   *
   * @param ref The type reference for serialization from and to.
   * @param <T> Resource handled by the implementing operations.
   * @return Instance which operates on database mechanisms and concerns.
   */
  <T> Mapper<T, UUID> from(final @lombok.NonNull Class<T> ref);

  /**
   * Provides generic database operations.
   *
   * @param <T> Resource handled by the implementing operations.
   * @param <I> Represents the domain context's identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Mapper<T, I> {

    Optional<T> getOne(final @lombok.NonNull I i);

    Map<I, T> getBy(final @lombok.NonNull Body<T> criteria,
                    final int skip, final int limit);

    I create(final @lombok.NonNull T t);

    boolean update(final @lombok.NonNull I i, final @lombok.NonNull T t);

    boolean delete(final @lombok.NonNull I i);
  }

  /**
   * Default abstraction for persistence handling concerns. Meant to be openly
   * extendable.
   *
   * @param <T> Resource from extension domain context handled by the
   *            implementing operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.Value(staticConstructor = "of")
  class Default<T> implements Mapper<T, UUID> {

    private static final String ID = "ID";

    DSLContext ctx;
    Class<T> ref;

    @Override
    public Optional<T> getOne(final @lombok.NonNull UUID uuid) {
      return ctx.select().from(DSL.table(ref.getSimpleName()))
                .where(DSL.field(ID).eq(uuid)).fetchOptional()
                .map(r -> Body.of(r.intoMap(), ref).toType());
    }

    @Override
    public Map<UUID, T> getBy(final @lombok.NonNull Body<T> criteria,
                              final int s, final int l) {
      return ctx
          .select().from(DSL.table(ref.getSimpleName()))
          .where(criteria.toMap().entrySet().stream().map(e -> DSL.condition(
              Operator.AND, DSL.field(e.getKey()).eq(e.getValue())))
                         .collect(Collectors.toList()))
          .stream().skip(s).limit(l).map(Record::intoMap).map(m -> {
            val id = UUID.fromString(m.remove(ID).toString());
            return Map.entry(id, Body.of(m, ref).toType());
          }).collect(Collectors.toMap(Map.Entry::getKey,
                                      Map.Entry::getValue));
    }

    @Override
    public UUID create(final @lombok.NonNull T t) {
      val m = Body.of(t).toMap();
      val id = UUID.randomUUID();
      m.put(ID, id.toString());
      val f = m.keySet().stream().map(DSL::field)
               .collect(Collectors.toList());
      val r = Exceptions.UNPROCESSABLE_ENTITY.trapIn(() -> ctx
          .insertInto(DSL.table(ref.getSimpleName()), f)
          .values(m.values()).execute());
      Exceptions.UNPROCESSABLE_ENTITY.throwIf(() -> 1 != r);
      return id;
    }

    @Override
    public boolean update(final @lombok.NonNull UUID id,
                          final @lombok.NonNull T t) {
      return 1 == ctx.update(DSL.table(ref.getSimpleName().toUpperCase()))
                     .set(Body.of(t).toMap())
                     .where(DSL.field(ID).eq(id)).execute();
    }

    @Override
    public boolean delete(final @lombok.NonNull UUID id) {
      return 1 == ctx.delete(DSL.table(ref.getSimpleName().toUpperCase()))
                     .where(DSL.field(ID).eq(id)).execute();
    }
  }
}
