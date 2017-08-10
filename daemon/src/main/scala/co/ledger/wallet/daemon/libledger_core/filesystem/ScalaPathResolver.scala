package co.ledger.wallet.daemon.libledger_core.filesystem

import java.io.File

import co.ledger.core.PathResolver

class ScalaPathResolver(poolName: String) extends PathResolver {
  /**
    * Resolves the path for a SQLite database file.
    *
    * @param path The path to resolve.
    * @return The resolved path.
    */
  override def resolveDatabasePath(path: String): String = resolve(path)

  /**
    * Resolves the path of a single log file.
    *
    * @param path The path to resolve.
    * @return The resolved path.
    */
  override def resolveLogFilePath(path: String): String = resolve(path)

  /**
    * Resolves the path for a json file.
    *
    * @param path The path to resolve.
    * @return The resolved path.
    */
  override def resolvePreferencesPath(path: String): String = resolve(path)

  private def resolve(path: String) = {
    val resolvedPath = new File(installDirectory, path)
    resolvedPath.mkdirs()
    resolvedPath.getAbsolutePath
  }
  private lazy val installDirectory: File = {
    import java.net.URLDecoder
    val jarPath = URLDecoder.decode(classOf[Nothing].getProtectionDomain.getCodeSource.getLocation.getPath, "UTF-8")
    new File(new File(jarPath).getParentFile, poolName)
  }
}
