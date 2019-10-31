# To do

# To not do

## Add interface to facade Web3j and Ethereum

To replace web3j entirely it is required to either write a lot of boilerplate
code or expose web3j explicitly. Thus such interface doesn't add any value but
adds compexity. It also make interface ambiguous: you need to decide use web3j
directly or get web3j from interface and use it.

## Make MPE contract address part of a channel id

Main usecase is to work with single MPE contract copy. Two or more MPE contracts
may be needed for rare channel migration usecases. So it will make API more
complex but doesn't have big value.
