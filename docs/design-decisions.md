# To do

## Add interface to facade Web3j and Ethereum

Ethereum interface is added to have single place for Ethereum related call
optimizations. For example frequently used "get last Ethereum block number"
operation could cache results thus decreasing response time.

# To not do

## Make MPE contract address part of a channel id

Main usecase is to work with single MPE contract copy. Two or more MPE contracts
may be needed for rare channel migration usecases. So it will make API more
complex but doesn't have big value.
