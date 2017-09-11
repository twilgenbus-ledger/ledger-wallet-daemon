package co.ledger.wallet.daemon.converters

import javax.inject.{Singleton}

import co.ledger.core.WalletPool
import co.ledger.wallet.daemon.models
import com.twitter.util.Future
import co.ledger.core.implicits._
import co.ledger.wallet.daemon.utils._
import scala.concurrent.ExecutionContext.Implicits.global
@Singleton
class PoolConverter {

  def apply(pool: WalletPool): Future[models.Pool] = pool.getWalletCount().map(models.Pool(pool.getName, _)).asTwitter().map {(p) =>
    println(s"Mapping ${p.name} ${p.wallet_count}")
    p
  }

}
