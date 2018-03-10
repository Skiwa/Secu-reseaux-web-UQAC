package ca.uqac.inf135.group3.tp3.jwt;

import ca.uqac.inf135.group3.tp3.tools.json.JSON;
import ca.uqac.inf135.group3.tp3.tools.json.JSONException;
import ca.uqac.inf135.group3.tp3.tools.json.JSONObject;
import ca.uqac.inf135.group3.tp3.tools.json.JSONParser;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class JwtToken {
    private static final String TOKEN_ALGO = "HS256";
    private static final String TOKEN_TYPE = "JWT";
    private static final String MAC_ALGO = "HmacSHA256";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final String header;    //JSON's string
    private final String payload;   //JSON's string
    private final byte[] signature; //hmac's bytes

    private static JSON getJSONFromString(String jsonStr) {
        if (jsonStr != null) {
            try {
                return new JSONParser(jsonStr).getJSON();
            } catch (JSONException e) {
                //Nothing more to do, let the default return do it's job
            }
        }
        return null;
    }

    private static byte[] getJsonBytes(JSON json) {
        if (json != null) {
            return json.toString().getBytes(CHARSET);
        }
        return null;
    }
    private static String getBase64(byte[] bytes) {
        if (bytes != null) {
            try {
                return Base64.getUrlEncoder().encodeToString(bytes);
            }
            catch (Exception e) {
                //Nothing more to do, let the default return do it's job
            }
        }
        return null;
    }

    private static byte[] parseBase64(String base64) {
        if (base64 != null) {
            try {
                return Base64.getUrlDecoder().decode(base64);
            }
            catch (Exception e) {
                //Nothing more to do, let the default return do it's job
            }
        }
        return null;
    }
    private static String getStringFromBytes(byte[] bytes) {
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }

    public JwtToken(JSON payload, byte[] secret) {
        this.header = new JSONObject()
                .add("alg", TOKEN_ALGO)
                .add("typ", TOKEN_TYPE)
                .toString();
        this.payload = payload.toString();

        this.signature = computeSignature(secret);
    }
    public JwtToken(String token) {
        if (token != null) {
            final String[] parts = token.split("\\.");

            if (parts.length == 3) {
                //We must parse Base64 first
                this.header = getStringFromBytes(parseBase64(parts[0]));
                this.payload = getStringFromBytes(parseBase64(parts[1]));
                this.signature = parseBase64(parts[2]);
                return;
            }
        }
        this.header = null;
        this.payload = null;
        this.signature = null;
    }

    private JSONObject getJSONHeader() {
        return (JSONObject) getJSONFromString(header);
    }

    public JSONObject getJSONPayload() {
        return (JSONObject) getJSONFromString(payload);
    }

    private String getBase64Header() {
        return getBase64(getJsonBytes(getJSONHeader()));
    }

    private String getBase64Payload() {
        return getBase64(getJsonBytes(getJSONPayload()));
    }

    private String getBase64Signature() {
        return getBase64(this.signature);
    }

    private byte[] computeSignature(byte[] secret) {
        try {
            //Concat header and payload
            byte[] headerAndPayload;
            headerAndPayload = String.format("%s.%s", getBase64Header(), getBase64Payload()).getBytes(CHARSET);

            //Initialize HASH_MAC algo and key
            final Mac macAlgo = Mac.getInstance(MAC_ALGO);

            SecretKeySpec secret_key = new SecretKeySpec(secret, MAC_ALGO);
            macAlgo.init(secret_key);

            //Return Hash of the header and payload, hashed with secret key
            return macAlgo.doFinal(headerAndPayload);
        }
        catch (Exception e) {
            //No matter what exception may have happen, we return null
            return null;
        }
    }

    public boolean isValid(byte[] secret) {
        //Validate header
        if (header == null) {
            return false;
        }

        final JSONObject jsonHeader = getJSONHeader();

        if (jsonHeader == null) {
            return false;
        }
        if (jsonHeader.count() == 2) {
            if (!TOKEN_ALGO.equals(jsonHeader.getString("alg"))) {
                return false;
            }
            if (!TOKEN_TYPE.equals(jsonHeader.getString("typ"))) {
                return false;
            }
        }
        //OK header is valid

        //Validate payload
        if (payload == null) {
            return false;
        }

        final JSONObject jsonPayload = getJSONPayload();

        if (jsonPayload == null) {
            return false;
        }

        //Validate username presence
        if (!jsonPayload.containsKey("username")) {
            return false;
        }

        String username = jsonPayload.getString("username");
        if (username == null) {
            return false;
        }
        if ("".equals(username)) {
            return false;
        }

        //Validate signature
        if (signature == null) {
            return false;
        }

        //Recompute signature using secret key
        byte[] localeSignature = computeSignature(secret);

        //If signature and localeSignature are identical, token is valid
        return Arrays.equals(signature, localeSignature);
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", getBase64Header(), getBase64Payload(), getBase64Signature());
    }
}
