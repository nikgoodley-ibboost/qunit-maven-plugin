package org.moyrax.resolver;

/**
 * This interface may be implemented to create a Resource Resolver. This kind
 * of objects helps searching for resources in different locations.
 *
 * @author Matias Mirabelli <lumen.night@gmail.com>
 * @since 1.1
 */
public interface ResourceResolver {
  /** It's returned if this resolved cannot handle the specified resource. */
  public static final int CANNOT_HANDLE = 0;

  /** It's returned if the resource can be handled by this resolver, and it's
      possible to allow other resolvers to proccess it. */
  public static final int HANDLE_SHARED = 1;

  /** It's returned if the resource can be handled by this resolver, and it
      cannot be proccessed by any other resolver. */
  public static final int HANDLE_EXCLUSIVE = 2;

  /**
   * Returns a value that determines if the resource can be handled by this
   * resolver, and how the resource should be loaded.
   *
   * @param uri Location of the resource to be loaded. It cannot be null.
   *
   * @return {@link ResourceResolver#CANNOT_HANDLE} ||
   *    {@link ResourceResolver#HANDLE_SHARED} ||
   *    {@link ResourceResolver#HANDLE_EXCLUSIVE}
   */
  public int canHandle(final String uri);

  /**
   * Resolves the specified resource and returns <code>true</code> if it was
   * successfully proccessed.
   *
   * @param uri Location of the resource to be handled. It cannot be null.
   *
   * @return The result depends on the resolver implementation. For more
   *    information see the class documentation.
   */
  public Object resolve(final String uri);
}
