package com.alarm.adapter.zcu104;

import com.alarm.adapter.RowhammerAdapter;
import com.alarm.alphabets.HammerAction;
import com.alarm.alphabets.HammerResult;
import com.alarm.alphabets.HammerRowsOutput;
import com.alarm.config.AdapterConfig;
import com.alarm.utils.TestRunnerException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.automatalib.words.Word;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.stream.Collectors;


// TODO: simplify tests?
//       - Group tests to same row together, adding up their readcounts
//       - Exploit symmetry? E.g.: [0 2] and [1 0] are the same: achievable by sorting?
//       - Use cache to extract results for queries are the same up to the two preceding items?
public class ZCU104Adapter extends RowhammerAdapter<HammerRowsOutput> {
    private final PrintWriter out;
    private final BufferedReader in;


    public ZCU104Adapter(AdapterConfig config) throws IOException {
        // Use this to set up SSH tunnel
        // ssh -f -N -L 4343:127.0.0.1:4343 -p 2222 uhac206@rhulhammer.rhul.io
        super(config);
        try (Socket boardConnection = new Socket("127.0.0.1", 4343)) {
            out = new PrintWriter(boardConnection.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(boardConnection.getInputStream()));
        }
    }

    @Override
    public void setup() {

    }

    @Override
    public void cleanup() {

    }

    @Override
    public HammerRowsOutput runTest(Word<HammerAction> test) throws TestRunnerException {
        String testString = test
                .stream()
                .map(HammerAction::toString)
                .collect(Collectors.joining(" "));

        System.out.println(testString);
        out.println(testString);
        HammerRowsOutput response;
        try {
            String jsonResponse = in.readLine();
            System.out.println(jsonResponse);
            response = processJSONResponse(jsonResponse);
        }
        catch(IOException e) {
            throw new TestRunnerException(testString);
        }

        return response;
    }

    private HammerRowsOutput processJSONResponse(String response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<Integer, Integer> rowFlipsMap = objectMapper.readValue(response, new TypeReference<>() {
        });

        if (!rowFlipsMap.isEmpty()) {
            return new HammerRowsOutput(HammerResult.FLIP).withFlipsLocations(rowFlipsMap.keySet());
        }

        return new HammerRowsOutput(HammerResult.OK);
    }

    public static void main(String[] args) throws IOException {
        ZCU104Adapter a = new ZCU104Adapter(null);
        try {
            a.runTest(Word.fromSymbols(new HammerAction(0, 50000, 1),
                    new HammerAction(1, 50000, 1)));
        }
        catch(TestRunnerException e){e.printStackTrace();}
    }
}
