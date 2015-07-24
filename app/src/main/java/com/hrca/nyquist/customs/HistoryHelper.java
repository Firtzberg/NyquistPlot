package com.hrca.nyquist.customs;

import android.content.Context;
import android.os.Bundle;

import com.hrca.nyquist.customs.displaycustoms.PolynomialView;
import com.hrca.nyquist.customs.displaycustoms.TransferFunctionView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Hrvoje on 21.1.2015..
 */
public class HistoryHelper {
    public static final String HISTORY_FILE = "history.xml";
    protected static final String XML_TRANSFER_FUNCTION = TransferFunctionView.PARCELABLE_TRANSFER_FUNCTION;
    protected static final String XML_GAIN = TransferFunctionView.PARCELABLE_GAIN_KEY;
    protected static final String XML_ASTATISM_VIEW = TransferFunctionView.PARCELABLE_ASTATISM_KEY;
    protected static final String XML_ASTATISM = AstatismView.PARCELABLE_ASTATISM_KEY;
    protected static final String XML_NUMERATOR_CHAIN = TransferFunctionView.PARCELABLE_NUMERATOR_KEY;
    protected static final String XML_DENOMINATOR_CHAIN = TransferFunctionView.PARCELABLE_DENOMINATOR_KEY;
    protected static final String XML_POLYNOMIAL = "polynomial";
    protected static final String XML_POLYNOMIAL_ELEMENT = "polynomialElement";
    protected static final String XML_EXPONENT = PolynomialElementBaseView.PARCELABLE_EXPONENT_KEY;
    protected static final String XML_NUMERATOR = PolynomialElementBaseView.PARCELABLE_NUMERATOR_KEY;
    protected static final String XML_DENOMINATOR = PolynomialElementBaseView.PARCELABLE_DENOMINATOR_KEY;
    public static final int MAX_HISTORY_LENGTH = 10;

        public static ArrayList<Bundle> getHistory(Context context){
            InputStream input;
            ArrayList<Bundle> history = new ArrayList<>();
            try {
                input = context.openFileInput(HISTORY_FILE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return history;
            }
            Bundle tf;
            try {
                XmlPullParserFactory xmlFactoryObject = XmlPullParserFactory
                        .newInstance();
                XmlPullParser parser = xmlFactoryObject.newPullParser();
                parser.setInput(input, null);
                int event = parser.next();
                /*if (event != XmlPullParser.START_TAG
                        || !parser.getName().equals(TransferFunctionView.PARCELABLE_TRANSFER_FUNCTION))
                    return null;*/
                //event = parser.nextTag();
                while (!(event == XmlPullParser.END_TAG && parser.getName().equals(
                        XML_TRANSFER_FUNCTION))) {
                    tf = HistoryHelper.getTransferFunction(parser);
                    if (tf != null)
                        history.add(0, tf);
                    if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                        break;
                    event = parser.next();
                }
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return history;
        }

        private static Bundle getTransferFunction(XmlPullParser parser) throws XmlPullParserException, IOException {
            if (parser.getEventType() != XmlPullParser.START_TAG
                    || !parser.getName().equals(XML_TRANSFER_FUNCTION))
                return null;
            Bundle tf = new Bundle();
            double gain;
            int event = parser.next();
            while (!(event == XmlPullParser.END_TAG && parser.getName().equals(
                    XML_TRANSFER_FUNCTION))) {
                if (event == XmlPullParser.START_TAG) {
                    if (parser.getName().equals(XML_GAIN)) {
                        gain = 1;
                        try {
                            gain = Double.parseDouble(parser.nextText());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        tf.putDouble(TransferFunctionView.PARCELABLE_GAIN_KEY, gain);
                    }
                    if (parser.getName().equals(XML_ASTATISM_VIEW)) {
                        tf.putParcelable(TransferFunctionView.PARCELABLE_ASTATISM_KEY,
                                HistoryHelper.getAstatism(parser));
                    }
                    if (parser.getName().equals(XML_NUMERATOR_CHAIN)) {
                        tf.putParcelable(TransferFunctionView.PARCELABLE_NUMERATOR_KEY,
                                HistoryHelper.getPolynomialChain(parser));
                    }
                    if (parser.getName().equals(XML_DENOMINATOR_CHAIN)) {
                        tf.putParcelable(TransferFunctionView.PARCELABLE_DENOMINATOR_KEY,
                                HistoryHelper.getPolynomialChain(parser));
                    }
                }
                if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                    break;
                event = parser.next();
            }
            return tf;
        }

    private static Bundle getAstatism(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG
                || !parser.getName().equals(XML_ASTATISM_VIEW))
            return null;
        Bundle astatism = new Bundle();
        int power;
        int event = parser.next();
        while (!(event == XmlPullParser.END_TAG && parser.getName().equals(
                XML_ASTATISM_VIEW))) {
            if (event == XmlPullParser.START_TAG) {
                if (parser.getName().equals(XML_ASTATISM)) {
                    power = 1;
                    try {
                        power = Integer.parseInt(parser.nextText());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    astatism.putInt(AstatismView.PARCELABLE_ASTATISM_KEY, power);
                }
            }
            if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                break;
            event = parser.next();
        }
        return astatism;
    }

    private static Bundle getPolynomialChain(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG)
            return null;
        ArrayList<Bundle> polynomials = new ArrayList<>();
        int event = parser.next();
        while (!(event == XmlPullParser.END_TAG &&
                (parser.getName().equals(XML_DENOMINATOR_CHAIN) ||
                parser.getName().equals(XML_NUMERATOR_CHAIN)))) {
            if (event == XmlPullParser.START_TAG) {
                if (parser.getName().equals(XML_POLYNOMIAL)) {
                    polynomials.add(HistoryHelper.getPolynomial(parser));
                }
            }
            if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                break;
            event = parser.next();
        }
        Bundle[] array = new Bundle[polynomials.size()];
        for(int i = 0; i < polynomials.size(); i ++)
            array[i] = polynomials.get(i);
        Bundle list = new Bundle();
        list.putParcelableArray(PolynomialChainView.PARCELABLE_POLYNOMIAL_LIST_KEY, array);
        return list;
    }

    private static Bundle getPolynomial(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG
                || !parser.getName().equals(XML_POLYNOMIAL))
            return null;
        ArrayList<Bundle> elements = new ArrayList<>();
        int event = parser.next();
        while (!(event == XmlPullParser.END_TAG && parser.getName().equals(
        XML_POLYNOMIAL))) {
            if (event == XmlPullParser.START_TAG) {
                if (parser.getName().equals(XML_POLYNOMIAL_ELEMENT)) {
                    elements.add(HistoryHelper.getPolynomialElement(parser));
                }
            }
            if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                break;
            event = parser.next();
        }
        Bundle[] array = new Bundle[elements.size()];
        for(int i = 0; i < elements.size(); i ++)
            array[i] = elements.get(i);
        Bundle list = new Bundle();
        list.putParcelableArray(PolynomialBaseView.PARCELABLE_ELEMENT_LIST_KEY, array);
        return list;
    }

    private static Bundle getPolynomialElement(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG
                || !parser.getName().equals(XML_POLYNOMIAL_ELEMENT))
            return null;
        Bundle element = new Bundle();
        double value;
        int exponent;
        int event = parser.next();
        while (!(event == XmlPullParser.END_TAG && parser.getName().equals(
                XML_POLYNOMIAL_ELEMENT))) {
            if (event == XmlPullParser.START_TAG) {
                if (parser.getName().equals(XML_NUMERATOR)) {
                    value = 1;
                    try {
                        value = Double.parseDouble(parser.nextText());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    element.putDouble(PolynomialElementBaseView.PARCELABLE_NUMERATOR_KEY, value);
                }
                if (parser.getName().equals(XML_DENOMINATOR)) {
                    value = 1;
                    try {
                        value = Double.parseDouble(parser.nextText());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    element.putDouble(PolynomialElementBaseView.PARCELABLE_DENOMINATOR_KEY, value);
                }
                if (parser.getName().equals(XML_EXPONENT)) {
                    exponent = 1;
                    try {
                        exponent = Integer.parseInt(parser.nextText());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    element.putInt(PolynomialElementBaseView.PARCELABLE_EXPONENT_KEY, exponent);
                }
            }
            if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                break;
            event = parser.next();
        }
        return element;
    }

    public static void add(TransferFunctionBaseView tf){
        FileOutputStream out;
        try{
            FileInputStream in = tf.getContext().openFileInput(HISTORY_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String content;
            while((content = reader.readLine()) != null)
                sb.append(content);
            reader.close();
            in.close();

            content = sb.toString();
            String startTag = '<' + XML_TRANSFER_FUNCTION + '>';
            int startTagPosition = 0;
            ArrayList<Integer> startTagPositions = new ArrayList<>(MAX_HISTORY_LENGTH);
            while(startTagPosition != -1){
                startTagPositions.add(startTagPosition);
                startTagPosition = content.indexOf(startTag, startTagPosition + startTag.length());
            }

            if(startTagPositions.size() >= MAX_HISTORY_LENGTH){
                out = tf.getContext().openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE);
                out.write(content.substring(startTagPositions.get(startTagPositions.size() - MAX_HISTORY_LENGTH + 1)).getBytes());
                out.close();
            }
        } catch (FileNotFoundException e) {
            //no history, no file, OK
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out = tf.getContext().openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE | Context.MODE_APPEND);
            writeTag(out, XML_TRANSFER_FUNCTION, true);

            writeTag(out, XML_GAIN, true);
            out.write(Double.toString(tf.getGain()).getBytes());
            writeTag(out, XML_GAIN, false);
            writeTag(out, XML_ASTATISM_VIEW, true);
            writeAstatism(tf.astatismView, out);
            writeTag(out, XML_ASTATISM_VIEW, false);
            writeTag(out, XML_NUMERATOR_CHAIN, true);
            writePolynomialChain(tf.numeratorChainView, out);
            writeTag(out, XML_NUMERATOR_CHAIN, false);
            writeTag(out, XML_DENOMINATOR_CHAIN, true);
            writePolynomialChain(tf.denominatorChainView, out);
            writeTag(out, XML_DENOMINATOR_CHAIN, false);

            writeTag(out, XML_TRANSFER_FUNCTION, false);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private static void writeTag(FileOutputStream out, String tag, boolean open) throws IOException {
        out.write('<');
        if(!open)
            out.write('/');
        out.write(tag.getBytes());
        out.write('>');
    }

    private static void writeAstatism(AstatismView astatism, FileOutputStream out) throws IOException {
        writeTag(out, XML_ASTATISM, true);
        out.write(Integer.toString(astatism.getAstatism()).getBytes());
        writeTag(out, XML_ASTATISM, false);
    }

    private static void writePolynomialChain(PolynomialChainView chain, FileOutputStream out) throws IOException {
        for(int i = 0; i < chain.list.size(); i++) {
            writeTag(out, XML_POLYNOMIAL, true);
            writePolynomial(chain.list.get(i), out);
            writeTag(out, XML_POLYNOMIAL, false);
        }
    }

    private static void writePolynomial(PolynomialView polynomial, FileOutputStream out) throws IOException {
        for(int i = 0; i < polynomial.list.size(); i++) {
            writeTag(out, XML_POLYNOMIAL_ELEMENT, true);
            writePolynomialElement(polynomial.list.get(i), out);
            writeTag(out, XML_POLYNOMIAL_ELEMENT, false);
        }
    }

    private static void writePolynomialElement(PolynomialElementBaseView element, FileOutputStream out) throws IOException {
        writeTag(out, XML_NUMERATOR, true);
        out.write(Double.toString(element.getNumerator()).getBytes());
        writeTag(out, XML_NUMERATOR, false);
        writeTag(out, XML_DENOMINATOR, true);
        out.write(Double.toString(element.getDenominator()).getBytes());
        writeTag(out, XML_DENOMINATOR, false);
        writeTag(out, XML_EXPONENT, true);
        out.write(Integer.toString(element.getExponent()).getBytes());
        writeTag(out, XML_EXPONENT, false);
    }

    public static void clear(Context context) {
        context.deleteFile(HISTORY_FILE);
    }

    public static void remove(int position, Context context){
        if(position < 0)
            return;
        FileOutputStream out;
        try{
            FileInputStream in = context.openFileInput(HISTORY_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String content;
            while((content = reader.readLine()) != null)
                sb.append(content);
            reader.close();
            in.close();

            content = sb.toString();
            String startTag = '<' + XML_TRANSFER_FUNCTION + '>';
            int startTagPosition = 0;
            ArrayList<Integer> startTagPositions = new ArrayList<>(MAX_HISTORY_LENGTH);
            while(startTagPosition != -1){
                startTagPositions.add(startTagPosition);
                startTagPosition = content.indexOf(startTag, startTagPosition + startTag.length());
            }

            if(position > startTagPositions.size())
                return;

            position = startTagPositions.size() - position - 1;
            out = context.openFileOutput(HISTORY_FILE, Context.MODE_PRIVATE);
            out.write(content.substring(0, startTagPositions.get(position)).getBytes());
            position++;
            if(position < startTagPositions.size())
                out.write(content.substring(startTagPositions.get(position)).getBytes());
            out.close();
        } catch (FileNotFoundException e) {
            //no history, no file, OK
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
