package co.ledger.wallet.daemon.services

import java.util.UUID
import javax.inject.{Inject, Singleton}

import co.ledger.wallet.daemon.database.{DaemonCache, UserDto}
import co.ledger.wallet.daemon.models.{AccountDerivationView, AccountView, PackedOperationsView}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountsService @Inject()(defaultDaemonCache: DaemonCache) extends DaemonService {

  def accounts(user: UserDto, poolName: String, walletName: String)
              (implicit ec: ExecutionContext): Future[Seq[AccountView]] = {
    info(LogMsgMaker.newInstance("Obtain accounts with params")
      .append("pool_name", poolName)
      .append("wallet_name", walletName)
      .append("user_pub_key", user.pubKey)
      .toString())
    defaultDaemonCache.getAccounts(user.pubKey, poolName, walletName).flatMap { wallets =>
      Future.sequence(wallets.map { wallet => wallet.accountView })
    }
  }

  def account(accountIndex: Int, user: UserDto, poolName: String, walletName: String)
             (implicit ec: ExecutionContext): Future[Option[AccountView]] = {
    info(LogMsgMaker.newInstance("Obtain account with params")
      .append("account_index", accountIndex)
      .append("pool_name", poolName)
      .append("wallet_name", walletName)
      .append("user_pub_key", user.pubKey)
      .toString())
    defaultDaemonCache.getAccount(accountIndex, user.pubKey, poolName, walletName).flatMap { accountOpt =>
      accountOpt match {
        case Some(account) => account.accountView.map(Option(_))
        case None => Future(None)
      }
    }
  }

  def nextAccountCreationInfo(user: UserDto, poolName: String, walletName: String, accountIndex: Option[Int])
                             (implicit ec: ExecutionContext): Future[AccountDerivationView] = {
    info(LogMsgMaker.newInstance("Obtain next available account creation information")
      .append("account_index", accountIndex)
      .append("pool_name", poolName)
      .append("wallet_name", walletName)
      .append("user_pub_key", user.pubKey)
      .toString())
    defaultDaemonCache.getNextAccountCreationInfo(user.pubKey, poolName, walletName, accountIndex).map(_.view)
  }

  def accountOperation(user: UserDto, accountIndex: Int, poolName: String, walletName: String, queryParams: OperationQueryParams): Future[PackedOperationsView] = {
    info(LogMsgMaker.newInstance("Obtain account operations with params")
      .append("previous", queryParams.previous)
      .append("next", queryParams.next)
      .append("batch", queryParams.batch)
      .append("full_op", queryParams.fullOp)
      .append("account_index", accountIndex)
      .append("wallet_name", walletName)
      .append("pool_name", poolName)
      .toString())
    if(queryParams.next.isEmpty && queryParams.previous.isEmpty) {
      // new request
      defaultDaemonCache.getAccountOperations(user, accountIndex, poolName, walletName, queryParams.batch, queryParams.fullOp)
    } else if (queryParams.next.isDefined) {
      // next has more priority, using database batch instead queryParams.batch
      defaultDaemonCache.getNextBatchAccountOperations(user, accountIndex, poolName, walletName, queryParams.next.get, queryParams.fullOp)
    } else {
      defaultDaemonCache.getPreviousBatchAccountOperations(user, accountIndex, poolName, walletName, queryParams.previous.get, queryParams.fullOp)
    }
  }

  def createAccount(accountCreationBody: AccountDerivationView, user: UserDto, poolName: String, walletName: String)
                   (implicit ec: ExecutionContext): Future[AccountView] = {
    info(LogMsgMaker.newInstance("Create account with params")
      .append("account_derivations", accountCreationBody)
      .append("pool_name", poolName)
      .append("wallet_name", walletName)
      .append("user_pub_key", user.pubKey)
      .toString())
    defaultDaemonCache.createAccount(accountCreationBody, user, poolName, walletName).flatMap(_.accountView)
  }
}

case class OperationQueryParams(previous: Option[UUID], next: Option[UUID], batch: Int = 20, fullOp: Int = 0)