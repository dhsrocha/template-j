package template.base.contract;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.val;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Operator;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import template.base.Body;
import template.base.Exceptions;

/**
 * Data Access Object, ensembles persistence communication concerns.
 *
 * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
 */
public interface Dao {

  String ID = "ID";

  /**
   * Loads a mapping handler for a given database mapping type.
   *
   * @param ref Resource's type reference for <i>from/to</i> serialization.
   * @param <T> Resource handled by the implementing operations.
   * @return Instance which operates on database mechanisms and concerns.
   */
  <T> Mapper<T, UUID> from(final @lombok.NonNull Class<T> ref);

  /**
   * Loads a mapping handler for a given database mapping type.
   *
   * @param root    Identity which indexes an entity from the {@link T root
   *                domain} context.
   * @param ref     Resource's type reference for <i>from/to</i> serialization.
   * @param extRef  Resource's type reference for <i>from/to</i> serialization.
   * @param canBind States if the two resources can be bound themselves.
   * @param <T>     Resource which the association will be based on.
   * @param <U>     Resource handled by the following operations.
   * @return Instance which operates on database mechanisms and concerns.
   */
  <T, U> Mapper.Composed<U, UUID> from(final @lombok.NonNull UUID root,
                                       final @lombok.NonNull Class<T> ref,
                                       final @lombok.NonNull Class<U> extRef,
                                       final @lombok.NonNull Predicate<U> canBind);

  /**
   * Provides generic database operations.
   *
   * @param <T> Resource handled by the implementing operations.
   * @param <I> Represents the domain context's identity.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  interface Mapper<T, I> {

    Optional<T> get(final @lombok.NonNull I i);

    Map<I, T> get(final @lombok.NonNull Body<T> criteria,
                  final int skip, final int limit);

    I create(final @lombok.NonNull T t);

    boolean update(final @lombok.NonNull I i, final @lombok.NonNull T t);

    boolean delete(final @lombok.NonNull I i);

    /**
     * Provides generic database operations composed with two tables.
     *
     * @param <U> Resource from extension domain context handled by the
     *            implementing operations.
     * @param <I> Represents the domain context's identity.
     * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
     */
    interface Composed<U, I> {

      Optional<U> get(final @lombok.NonNull I i);

      Map<I, U> get(final @lombok.NonNull Body<U> criteria,
                    final int skip, final int limit);

      I create(final @lombok.NonNull U u);

      boolean link(final @lombok.NonNull I id);

      boolean unlink(final @lombok.NonNull I id);
    }
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

    DSLContext ctx;
    Class<T> ref;

    @Override
    public Optional<T> get(final @lombok.NonNull UUID uuid) {
      return ctx.select().from(DSL.table(nameOf(ref)))
                .where(DSL.field(ID).eq(uuid)).fetchOptional()
                .map(r -> Body.of(r.intoMap(), ref).toType());
    }

    @Override
    public Map<UUID, T> get(final @lombok.NonNull Body<T> criteria,
                            final int s, final int l) {
      return ctx
          .select().from(DSL.table(nameOf(ref)))
          .where(criteriaOf(criteria))
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
          .insertInto(DSL.table(nameOf(ref)), f).values(m.values()).execute());
      Exceptions.UNPROCESSABLE_ENTITY.throwIf(() -> 1 != r);
      return id;
    }

    @Override
    public boolean update(final @lombok.NonNull UUID id,
                          final @lombok.NonNull T t) {
      return 1 == ctx.update(DSL.table(nameOf(ref))).set(Body.of(t).toMap())
                     .where(DSL.field(ID).eq(id)).execute();
    }

    @Override
    public boolean delete(final @lombok.NonNull UUID id) {
      return 1 == ctx.delete(DSL.table(nameOf(ref)))
                     .where(DSL.field(ID).eq(id)).execute();
    }
  }

  /**
   * Abstraction for persistence handling concerns between two entities. Meant
   * to be openly extendable.
   *
   * @param <T> Resource which the association will be based on.
   * @param <U> Resource handled by the following operations.
   * @author <a href="mailto:dhsrocha.dev@gmail.com">Diego Rocha</a>
   */
  @lombok.Value(staticConstructor = "of")
  class Composed<T, U> implements Mapper.Composed<U, UUID> {

    UUID root;
    Predicate<U> canBind;
    DSLContext ctx;
    Class<T> base;
    Class<U> ext;

    @Override
    public Optional<U> get(final @lombok.NonNull UUID uuid) {
      return ctx.select().from(joined(base, ext))
                .where(DSL.field(ID).eq(uuid)).fetchOptional()
                .map(r -> Body.of(r.intoMap(), ext).toType());
    }

    @Override
    public Map<UUID, U> get(final @lombok.NonNull Body<U> criteria,
                            final int s, final int l) {
      return ctx
          .select().from(joined(base, ext)).where(criteriaOf(criteria))
          .stream().skip(s).limit(l).map(Record::intoMap).map(m -> {
            val id = UUID.fromString(m.remove(ID).toString());
            return Map.entry(id, Body.of(m, ext).toType());
          }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public UUID create(final @lombok.NonNull U u) {
      Exceptions.UNPROCESSABLE_ENTITY.throwIf(() -> !canBind.test(u));
      return ctx.dsl().transactionResult(tx -> {
        val b = Default.of(DSL.using(tx), ext);
        val c = Composed.of(root, canBind, DSL.using(tx), base, ext);
        val id = b.create(u);
        Exceptions.CANNOT_BIND_UNBIND.throwIf(() -> !c.link(id));
        return id;
      });
    }

    @Override
    public boolean link(final @lombok.NonNull UUID id) {
      val u = Default.of(ctx, ext).get(id).orElseThrow(Exceptions.NOT_FOUND);
      Exceptions.UNPROCESSABLE_ENTITY.throwIf(() -> !canBind.test(u));
      val v = Map.of(DSL.field(nameOf(base) + '_' + ID), root,
                     DSL.field(nameOf(ext) + '_' + ID), id);
      return 1 == Exceptions.CANNOT_BIND_UNBIND.trapIn(() -> ctx
          .insertInto(DSL.table(nameOf(base) + '_' + nameOf(ext)), v.keySet())
          .values(v.values()).execute());
    }

    @Override
    public boolean unlink(final @lombok.NonNull UUID id) {
      return 1 == Exceptions.CANNOT_BIND_UNBIND.trapIn(() -> ctx
          .deleteFrom(DSL.table(nameOf(base) + '_' + nameOf(ext)))
          .where(DSL.field(nameOf(base) + '_' + ID).eq(root))
          .and(DSL.field(nameOf(ext) + '_' + ID).eq(id)).execute());
    }

    /**
     * Creates a JOOQ join table reference.
     *
     * @param base Root domain context reference.
     * @param ext  Extension domain context reference.
     * @return Table reference to be used {@link Mapper} methods.
     */
    private static Table<Record> joined(final @lombok.NonNull Class<?> base,
                                        final @lombok.NonNull Class<?> ext) {
      val join = nameOf(base) + '_' + nameOf(ext);
      return DSL.table(nameOf(ext)).innerJoin(DSL.table(join))
                .on(DSL.field(nameOf(ext) + '.' + ID)
                       .eq(DSL.field(join + '.' + nameOf(ext) + '_' + ID)));
    }
  }

  /**
   * Creates an {@code WHERE} set for JOOQ queries.
   *
   * @param criteria Body to be parsed on.
   * @return Required parameters to be added on.
   */
  private static Collection<Condition> criteriaOf(
      final @lombok.NonNull Body<?> criteria) {
    return criteria.toMap().entrySet().stream().map(e -> DSL.condition(
        Operator.AND, DSL.field(DSL.name(e.getKey())).eq(e.getValue())))
                   .collect(Collectors.toSet());
  }

  /**
   * Standard way for tables and fields' names.
   *
   * @param ref Resource reference.
   * @return Standard name.
   */
  private static String nameOf(final @lombok.NonNull Class<?> ref) {
    return ref.getSimpleName().toUpperCase();
  }
}
