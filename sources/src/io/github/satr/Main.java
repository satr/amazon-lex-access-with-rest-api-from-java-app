package io.github.satr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.impl.MetadataMap;
import org.apache.geronimo.mail.util.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.apache.cxf.common.util.StringUtils.isEmpty;

public class Main {

    public static void main(String[] args) {
        try {
            final String method = "POST";
            final String service = "lex";
            final String host = "runtime.lex.us-east-1.amazonaws.com";
            final String region = "us-east-1";
            final String endpoint = "https://runtime.lex.us-east-1.amazonaws.com/";

            final String accessKey = "";// Put here user's AWS_ACCESS_KEY_ID
            final String secretKey = "";// Put here user's AWS_SECRET_ACCESS_KEY
            // Check out "Best Practices for Managing AWS Access Keys":
            // http://docs.aws.amazon.com/general/latest/gr/aws-access-keys-best-practices.html

            final String botName = "TestBotForRequest";
            final String botAlias = "testbotforrequest";
            final String userId = "myUserId";//some user id, which will be sent in a Lex request field "userId"
            final String postAction = "text";
            // for stream request (text or audio stream)
            //postAction = "content"

            // POST requests use a content type header. For Lex,
            // the content is JSON or stream.
            // for postAction = "text":
            final String contentType = "application/json";
            // for postAction = "content", audio stream in PCM format:
            //contentType = "audio/l16;rate=16000;channels=1"
            //contentType = "audio/x-l16;sample-rate=16000;channel-count=1"
            // for postAction = "content", audio stream in Opus format:
            //contentType = "audio/x-cbr-opus-with-preamble;preamble-size=0;bit-rate=256000;frame-size-milliseconds=4"
            // for postAction = "content", text:
            //contentType = "text/plain;charset=utf-8"

            final String canonicalUri = String.format("/bot/%s/alias/%s/user/%s/%s/", botName, botAlias, userId, postAction);//notice the leading "/"
            final String canonicalQueryString = "";//optional - usually not used in REST API-s
            final String signedHeaders = "content-type;host;x-amz-date";
            final String algorithm = "AWS4-HMAC-SHA256";

            final List<Object> providers = new ArrayList<>();
            providers.add(new JacksonJaxbJsonProvider());

            final WebClient client = WebClient.create(endpoint, providers);
            client.accept(contentType)
                    .type(contentType)
                    .path(canonicalUri);

            System.out.println("Ready.");
            final Scanner scanner = new Scanner(System.in);
            while(true) {
                String requestText = scanner.nextLine().trim();
                if(isEmpty(requestText))
                    break;


                final String requestParameters = String.format("{\"inputText\": \"%s\", \"sessionAttributes\": {\"attr_name\" : \"value\"}}", requestText);

                final String payloadHash = hexEncode(sha256Hash(requestParameters));
                final ZonedDateTime utcNow = Instant.now().atZone(ZoneOffset.UTC);// Date for headers and the credential string
                final String amzDate = utcNow.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
                final String dateStamp = utcNow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                final String canonicalHeaders = String.format("content-type:%s\nhost:%s\nx-amz-date:%s\n", contentType, host, amzDate);
                final String canonicalRequest = String.format("%s\n%s\n%s\n%s\n%s\n%s", method, canonicalUri, canonicalQueryString, canonicalHeaders, signedHeaders, payloadHash);
                final String credentialScope = String.format("%s/%s/%s/aws4_request", dateStamp, region, service);
                final String canonicalRequestHash = hexEncode(sha256Hash(canonicalRequest));
                final String stringToSign = String.format("%s\n%s\n%s\n%s", algorithm, amzDate, credentialScope, canonicalRequestHash);
                final byte[] signatureKey = getSignatureKey(secretKey, dateStamp, region, service);
                final String signature = hexEncode(HmacSHA256(stringToSign, signatureKey));
                final String authorizationHeader = String.format("%s Credential=%s/%s, SignedHeaders=%s, Signature=%s", algorithm, accessKey, credentialScope, signedHeaders, signature);
                final MetadataMap<String, String> headersMap = new MetadataMap<>();
                headersMap.add("Content-Type", contentType);
                headersMap.add("X-Amz-Date", amzDate);
                headersMap.add("Authorization", authorizationHeader);
                client.headers(headersMap);

                Response response = client.post(requestParameters);
                InputStream responseStream = (InputStream) response.getEntity();
                if(response.getStatus() == 200) {
                    JsonNode jsonNode = new ObjectMapper().readTree(responseStream);
                    String dialogState = jsonNode.get("dialogState").asText("");
                    if(dialogState.startsWith("Elicit"))
                        System.out.println(jsonNode.get("message").asText(""));
                    else if(dialogState.startsWith("ReadyForFulfillment"))
                        System.out.println(String.format("ReadyForFulfillment; Intent: %s; Slots: %s", jsonNode.get("intentName").asText(""), jsonNode.get("slots").toString()));
                } else {
                    JsonNode errorMessage = new ObjectMapper().readTree(responseStream).get("message");
                    if (errorMessage != null)
                        System.out.println(errorMessage);
                    List<Object> xAmznErrorType = response.getMetadata().get("x-amzn-ErrorType");
                    if (!xAmznErrorType.isEmpty())
                        System.out.println(xAmznErrorType.get(0));
                    List<Object> amznRequestId = response.getMetadata().get("x-amzn-RequestId");
                    if (!amznRequestId.isEmpty())
                        System.out.println(amznRequestId.get(0));
                }
            }
            System.out.println("Bye.");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] sha256Hash(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF8"));
    }

    private static String hexEncode(byte[] data) throws Exception {
        return new String(Hex.encode(data));
    }

    //Source: http://docs.aws.amazon.com/general/latest/gr/signature-v4-examples.html#signature-v4-examples-java
    static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        String algorithm="HmacSHA256";
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));
        return mac.doFinal(data.getBytes("UTF8"));
    }

    static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF8");
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }
}