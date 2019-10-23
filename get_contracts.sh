#!/bin/sh

# use npm view singularinet-platform-contracts to get url
platform_contracts_version=0.3.4
platform_contracts_tgz=https://registry.npmjs.org/singularitynet-platform-contracts/-/singularitynet-platform-contracts-$platform_contracts_version.tgz

mkdir -p target

cd target
wget $platform_contracts_tgz -O singularitynet-platform-contracts.tgz
tar -xzf ./singularitynet-platform-contracts.tgz
mv package singularitynet-platform-contracts

cd singularitynet-platform-contracts
mkdir ../generated-sources/sol/java
output=../generated-sources/sol/java
package=io.singularitynet.sdk.contracts
web3j solidity generate -a ./abi/MultiPartyEscrow.json --outputDir $output --package $package #--solidityTypes
web3j solidity generate -a ./abi/Registry.json --outputDir $output --package $package

cd ..
snet_daemon_version=2.0.2
snet_daemon_tgz=https://github.com/singnet/snet-daemon/releases/download/v$snet_daemon_version/snet-daemon-v$snet_daemon_version-linux-amd64.tar.gz
wget $snet_daemon_tgz -O snet-daemon.tar.gz
tar xzf snet-daemon.tar.gz
rm -rf snet-daemon
mv ./snet-daemon-v$snet_daemon_version-linux-amd64 snet-daemon
sed -i '8s/^/option java_package = "io.singularitynet.daemon.escrow";\n/' snet-daemon/proto/state_service.proto
sed -i '2s/^/option java_package = "io.singularitynet.daemon.escrow";\n/' snet-daemon/proto/control_service.proto
sed -i '2s/^/option java_package = "io.singularitynet.daemon.configuration";\n/' snet-daemon/proto/configuration_service.proto
