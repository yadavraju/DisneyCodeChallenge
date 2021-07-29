package co.betterup.betterfeed.api.repository

import kotlinx.coroutines.flow.flow
import retrofit2.Response

abstract class NetworkBoundRepository<RESULT> {

  /** Return flow data */
  fun asFlow() =
      flow<RESULT> {
        val apiResponse = fetchFromRemote()
        emit(apiResponse)
      }

  /** Fetches [Response] from the remote end point. */
  protected abstract suspend fun fetchFromRemote(): RESULT
}
