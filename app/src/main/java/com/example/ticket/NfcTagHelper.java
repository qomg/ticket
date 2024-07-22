package com.example.ticket;


import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.Nullable;

import java.lang.reflect.Method;

public class NfcTagHelper {
    static final int TECH_NFC_A = 1;
    static final String EXTRA_NFC_A_SAK = "sak";    // short (SAK byte value)
    static final String EXTRA_NFC_A_ATQA = "atqa";  // byte[2] (ATQA value)

    static final int TECH_NDEF = 6;
    static final String EXTRA_NDEF_MSG = "ndefmsg";              // NdefMessage (Parcelable)
    static final String EXTRA_NDEF_MAXLENGTH = "ndefmaxlength";  // int (result for getMaxSize())
    static final String EXTRA_NDEF_CARDSTATE = "ndefcardstate";  // int (1: read-only, 2: read/write, 3: unknown)
    static final String EXTRA_NDEF_TYPE = "ndeftype";            // int (1: T1T, 2: T2T, 3: T3T, 4: T4T, 101: MF Classic, 102: ICODE)

    @Nullable
    public static Intent makeIntent(String packageName) {
        try {
            Class<Tag> tagClass = Tag.class;
            Method createMockTagMethod = tagClass.getDeclaredMethod("createMockTag", byte[].class, int[].class, Bundle[].class, long.class);

            NdefMessage ndefMessage = new NdefMessage(NdefRecord.createMime("text/plain", "Text".getBytes("US-ASCII")));

            Bundle nfcaBundle = new Bundle();
            nfcaBundle.putByteArray(EXTRA_NFC_A_ATQA, new byte[]{(byte) 0x44, (byte) 0x00}); //ATQA for Type 2 tag
            nfcaBundle.putShort(EXTRA_NFC_A_SAK, (short) 0x00); //SAK for Type 2 tag

            Bundle ndefBundle = new Bundle();
            ndefBundle.putInt(EXTRA_NDEF_MAXLENGTH, 48); // maximum message length: 48 bytes
            ndefBundle.putInt(EXTRA_NDEF_CARDSTATE, 1); // read-only
            ndefBundle.putInt(EXTRA_NDEF_TYPE, 2); // Type 2 tag
            ndefBundle.putParcelable(EXTRA_NDEF_MSG, ndefMessage);  // add an NDEF message

            byte[] tagId = new byte[]{(byte) 0x3F, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x90, (byte) 0xAB};

            Tag mockTag = (Tag) createMockTagMethod.invoke(
                    null,
                    tagId,                                     // tag UID/anti-collision identifier (see Tag.getId() method)
                    new int[]{TECH_NFC_A, TECH_NDEF},       // tech-list
                    new Bundle[]{nfcaBundle, ndefBundle},
                    0
            );  // array of tech-extra bundles, each entry maps to an entry in the tech-list

            Intent ndefIntent = new Intent(NfcAdapter.ACTION_TECH_DISCOVERED);
            //        ndefIntent.setType("text/plain");
            ndefIntent.setType("application/vnd.com.example.ticket");
            ndefIntent.setPackage(packageName);
            ndefIntent.putExtra(NfcAdapter.EXTRA_ID, tagId);
            ndefIntent.putExtra(NfcAdapter.EXTRA_TAG, mockTag);
            ndefIntent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, new NdefMessage[]{ndefMessage});
            return ndefIntent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
