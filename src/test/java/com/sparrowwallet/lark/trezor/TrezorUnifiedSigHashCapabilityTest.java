package com.sparrowwallet.lark.trezor;

import com.google.protobuf.UnknownFieldSet;
import com.sparrowwallet.lark.trezor.generated.TrezorMessageManagement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Whether a device is read as able to honour the unified signature hash.
 *
 * This decides whether a transaction asking for the unified opt-in is handed to the device or refused, and getting it
 * wrong in the permissive direction is the expensive way: the device would sign the legacy message, the signature would
 * carry no replay protection, and nothing downstream would say so. So the read is pinned rather than reviewed.
 *
 * Two things about it are easy to get wrong and are covered here individually.
 */
public class TrezorUnifiedSigHashCapabilityTest {
    /** The capability number the firmware reports. Not 28: upstream assigned that to Ethereum EIP7702. */
    private static final int UNIFIED_SIGHASH = 29;
    private static final int ETHEREUM_EIP7702 = 28;
    private static final int CAPABILITIES_FIELD = 30;

    @Test
    public void noFeaturesReadsAsUnsupported() {
        Assertions.assertFalse(TrezorDevice.supportsUnifiedSigHash(null),
                "a device that has not been asked yet must not be treated as able to honour the opt-in");
    }

    @Test
    public void featuresWithoutTheCapabilityReadAsUnsupported() {
        Assertions.assertFalse(TrezorDevice.supportsUnifiedSigHash(features().build()));
    }

    /**
     * The case that matters most: firmware claiming Ethereum EIP7702, which upstream numbered 28, must not be read as
     * claiming the unified sighash. Matching 28 as well would read the opt-in off firmware that never offered it, and
     * every transaction signed under it would fail to verify.
     */
    @Test
    public void theAdjacentEthereumCapabilityIsNotMistakenForIt() {
        Assertions.assertFalse(TrezorDevice.supportsUnifiedSigHash(withUnknownCapability(ETHEREUM_EIP7702)),
                "capability " + ETHEREUM_EIP7702 + " is Ethereum EIP7702, not the unified signature hash");
    }

    /**
     * The realistic supported case. Firmware newer than the protobuf generated into this tree reports a capability this
     * build has no enum constant for, and proto2 keeps such a value in the unknown fields rather than discarding it.
     * Reading only the parsed list would call that firmware unsupported and refuse a device that can in fact honour it.
     */
    @Test
    public void anUnrecognisedCapabilityValueIsStillRead() {
        Assertions.assertTrue(TrezorDevice.supportsUnifiedSigHash(withUnknownCapability(UNIFIED_SIGHASH)),
                "a capability this build has no constant for must still be read out of the unknown fields");
    }

    /** And the same value once a future regeneration gives it a constant, so the parsed list is read too. */
    @Test
    public void aRecognisedCapabilityValueIsRead() {
        TrezorMessageManagement.Features.Capability constant = null;
        for(TrezorMessageManagement.Features.Capability c : TrezorMessageManagement.Features.Capability.values()) {
            if(c.getNumber() == UNIFIED_SIGHASH) {
                constant = c;
            }
        }
        if(constant == null) {
            //No constant in this generation, which the unknown fields case above already covers.
            return;
        }

        Assertions.assertTrue(TrezorDevice.supportsUnifiedSigHash(features().addCapabilities(constant).build()));
    }

    /** proto2 marks the version triple required, so a Features cannot be built without one. */
    private static TrezorMessageManagement.Features.Builder features() {
        return TrezorMessageManagement.Features.newBuilder().setMajorVersion(2).setMinorVersion(9).setPatchVersion(0);
    }

    /** Features carrying a capability the device really reports, as an unrecognised proto2 enum value. */
    private static TrezorMessageManagement.Features withUnknownCapability(int number) {
        return features()
                .setUnknownFields(UnknownFieldSet.newBuilder()
                        .addField(CAPABILITIES_FIELD, UnknownFieldSet.Field.newBuilder().addVarint(number).build())
                        .build())
                .build();
    }
}
