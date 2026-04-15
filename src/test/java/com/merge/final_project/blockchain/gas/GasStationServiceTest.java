package com.merge.final_project.blockchain.gas;

import com.merge.final_project.blockchain.security.WalletPrivateKeyResolver;
import com.merge.final_project.blockchain.service.TransferTransactionService;
import com.merge.final_project.blockchain.tx.BlockchainTransferClient;
import com.merge.final_project.blockchain.tx.TransferResult;
import com.merge.final_project.blockchain.wallet.HotWalletResolver;
import com.merge.final_project.wallet.entity.Wallet;
import com.merge.final_project.wallet.entity.WalletType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GasStationServiceTest {

    private static final String HOT_ADDRESS = "0xhot";
    private static final String USER_ADDRESS = "0xuser";
    private static final BigInteger GAS_PRICE_WEI = new BigInteger("30000000000"); // 30 gwei
    private static final BigInteger INITIAL_POL_WEI = new BigInteger("30000000000000000"); // 0.03 POL
    private static final BigInteger MIN_POL_WEI = new BigInteger("10000000000000000"); // 0.01 POL
    private static final BigInteger TOPUP_HEADROOM_WEI = new BigInteger("5000000000000000"); // 0.005 POL
    private static final BigInteger TX_GAS_LIMIT = new BigInteger("300000");

    @Mock
    private HotWalletResolver hotWalletResolver;
    @Mock
    private BlockchainTransferClient blockchainTransferClient;
    @Mock
    private TransferTransactionService transferTransactionService;
    @Mock
    private WalletPrivateKeyResolver walletPrivateKeyResolver;
    @Mock
    private Web3j web3j;

    private GasStationService gasStationService;
    private Wallet hotWallet;
    private Wallet userWallet;

    @BeforeEach
    void setUp() {
        gasStationService = new GasStationService(
                hotWalletResolver,
                blockchainTransferClient,
                transferTransactionService,
                walletPrivateKeyResolver,
                web3j,
                INITIAL_POL_WEI.toString(),
                MIN_POL_WEI.toString(),
                "50000000000000000",
                TX_GAS_LIMIT.toString(),
                "12500",
                TOPUP_HEADROOM_WEI.toString(),
                "3",
                HOT_ADDRESS
        );

        hotWallet = Wallet.builder()
                .walletNo(1L)
                .walletType(WalletType.HOT)
                .walletAddress(HOT_ADDRESS)
                .build();
        userWallet = Wallet.builder()
                .walletNo(2L)
                .walletType(WalletType.USER)
                .walletAddress(USER_ADDRESS)
                .build();

        when(hotWalletResolver.resolve(HOT_ADDRESS)).thenReturn(hotWallet);
        when(walletPrivateKeyResolver.resolveForWallet(hotWallet)).thenReturn("decrypted-hot-private-key");
    }

    @Test
    void ensureSufficientPol_topsUpOnlyShortageToRequiredReserve() throws Exception {
        mockGasPrice(GAS_PRICE_WEI);
        BigInteger currentBalanceWei = new BigInteger("16000000000000000"); // 0.016 POL
        mockBalance(USER_ADDRESS, currentBalanceWei);
        when(blockchainTransferClient.transferNative(anyString(), anyString(), anyString(), any()))
                .thenReturn(successResult());

        gasStationService.ensureSufficientPol(userWallet);

        BigInteger requiredReserveWei = calculateRequiredReserveWei(GAS_PRICE_WEI);
        BigInteger expectedTopUpWei = requiredReserveWei.subtract(currentBalanceWei);

        ArgumentCaptor<BigInteger> amountCaptor = ArgumentCaptor.forClass(BigInteger.class);
        verify(blockchainTransferClient).transferNative(
                eq(HOT_ADDRESS),
                eq("decrypted-hot-private-key"),
                eq(USER_ADDRESS),
                amountCaptor.capture()
        );
        assertThat(amountCaptor.getValue()).isEqualTo(expectedTopUpWei);
        verify(transferTransactionService).saveTransfer(
                eq(hotWallet),
                eq(userWallet),
                eq(expectedTopUpWei.longValueExact()),
                eq("0xtx"),
                eq(123L),
                eq("SUCCESS"),
                eq("POL_AUTO_TOPUP")
        );
    }

    @Test
    void fundInitialPol_keepsInitialTargetWhenItIsHigherThanRequiredReserve() throws Exception {
        mockGasPrice(GAS_PRICE_WEI);
        when(blockchainTransferClient.transferNative(anyString(), anyString(), anyString(), any()))
                .thenReturn(successResult());

        gasStationService.fundInitialPol(userWallet);

        ArgumentCaptor<BigInteger> amountCaptor = ArgumentCaptor.forClass(BigInteger.class);
        verify(blockchainTransferClient).transferNative(
                eq(HOT_ADDRESS),
                eq("decrypted-hot-private-key"),
                eq(USER_ADDRESS),
                amountCaptor.capture()
        );
        assertThat(amountCaptor.getValue()).isEqualTo(INITIAL_POL_WEI);
        verify(transferTransactionService).saveTransfer(
                eq(hotWallet),
                eq(userWallet),
                eq(INITIAL_POL_WEI.longValueExact()),
                eq("0xtx"),
                eq(123L),
                eq("SUCCESS"),
                eq("POL_AUTO_TOPUP")
        );
    }

    private BigInteger calculateRequiredReserveWei(BigInteger gasPriceWei) {
        BigInteger baseRequiredWei = gasPriceWei.multiply(TX_GAS_LIMIT);
        BigInteger bufferedRequiredWei = baseRequiredWei.multiply(BigInteger.valueOf(12_500))
                .add(BigInteger.valueOf(9_999))
                .divide(BigInteger.valueOf(10_000));
        BigInteger dynamicRequiredWei = bufferedRequiredWei.add(TOPUP_HEADROOM_WEI);
        return MIN_POL_WEI.max(dynamicRequiredWei);
    }

    @SuppressWarnings("unchecked")
    private void mockGasPrice(BigInteger gasPriceWei) throws Exception {
        Request<?, EthGasPrice> gasPriceRequest = mock(Request.class);
        EthGasPrice gasPriceResponse = mock(EthGasPrice.class);
        doReturn(gasPriceRequest).when(web3j).ethGasPrice();
        when(gasPriceRequest.send()).thenReturn(gasPriceResponse);
        when(gasPriceResponse.getGasPrice()).thenReturn(gasPriceWei);
    }

    @SuppressWarnings("unchecked")
    private void mockBalance(String walletAddress, BigInteger balanceWei) throws Exception {
        Request<?, EthGetBalance> balanceRequest = mock(Request.class);
        EthGetBalance balanceResponse = mock(EthGetBalance.class);
        doReturn(balanceRequest)
                .when(web3j)
                .ethGetBalance(eq(walletAddress), eq(DefaultBlockParameterName.LATEST));
        when(balanceRequest.send()).thenReturn(balanceResponse);
        when(balanceResponse.getBalance()).thenReturn(balanceWei);
    }

    private TransferResult successResult() {
        return new TransferResult(
                "0xtx",
                123L,
                "SUCCESS",
                "ok",
                "POL_AUTO_TOPUP",
                HOT_ADDRESS,
                USER_ADDRESS,
                null,
                null,
                BigInteger.ONE
        );
    }
}
