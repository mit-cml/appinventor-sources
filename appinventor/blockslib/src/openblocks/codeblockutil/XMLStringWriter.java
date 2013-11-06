// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.codeblockutil;

/**
 * XMLStringWriter is a very simple and naive writer to create XML Strings that
 * are nicely formatted.  It's not very smart, so you need to be careful how you use
 * addDataElement, beginElement, endAttribute, endElement.  See the doc for these
 * methods as you use them.
 *
 */
public class XMLStringWriter {
    private StringBuffer contents = new StringBuffer();
    //contants
    private static final String space = " ";
    private static final String equals = "=";
    private static final String quotes = "\"";
    private static final String nextline = "\n";
    private static final String lt = "<";
    private static final String gt = ">";
    private static final String slashlt = "</";


    private String indent = space;
    private int currentIndent = 1;

    private String root = null;

    /**
     * Constructs a new XMLStringWriter that formats elements and attributes
     * appened to this writer instance.
     * @param indent the size of the indent for this writer
     */
    public XMLStringWriter(int indent){
        while(indent > 1){
            this.indent += space;
            indent--;
        }
    }

    /**
     * Constructs a new XMLStringWriter with a default indentation length of 4.
     */
    public XMLStringWriter() {
    	this(4);
    }

    /**
     * Appends the string constructed by the specified writer to this.
     * @param writer the desired XMLStringWriter to append to this
     */
    public void appendXMLWriterString(XMLStringWriter writer){
        appendIndent();
        contents.append(writer.toString());
    }

    private void appendBegElement(String ele){
        contents.append(lt);
        contents.append(ele);
        contents.append(gt);
    }

    private void appendEndElement(String ele){
        contents.append(slashlt);
        contents.append(ele);
        contents.append(gt);
    }

    private void appendIndent(){
        int temp = currentIndent;
        while(temp>1){
            contents.append(indent);
            temp--;
        }
    }

    /**
     * Begings this XML String.
     * Must call this first.
     * @param root
     */
    public void beginXMLString(String root){
        contents.append(lt);
        contents.append(root);
        contents.append(gt);

        contents.append(nextline);

        currentIndent++;

        this.root = root;
    }

    /**
     * Ends this XML String.
     * Must call this last.
     */
    public void endXMLString(){
        contents.append(slashlt);
        contents.append(this.root);
        contents.append(gt);
    }

    /**
     * Adding an element like this assumes that there are no
     * attributes to be added to this element
     * @param ele
     * @param text
     */
    public void addDataElement(String ele, String text){
        appendIndent();
        appendBegElement(ele);
        contents.append(text);
        appendEndElement(ele);
        contents.append(nextline);
    }

    /**
     * Adding an element like this or beginning an element tree.
     * If hasAttriutes is true, must call endAttributes when you're done
     * adding all attributes
     * In order to add elements, you must call endAttributes().
     * @param ele
     */
    public void beginElement(String ele, boolean hasAttributes){
        appendIndent();
        if(hasAttributes){
            contents.append(lt);
            contents.append(ele);
        }else{
            appendBegElement(ele);
            contents.append(nextline);
        }
        currentIndent++;
    }

    /**
     * Adds the specified data to the current XML string.
     * This data should be added between a begin and end Element tag
     * @param data String data to append to the current XML String
     */
    public void addElementTextData(String data) {
    	contents.append(data);
    }

    /**
     * Adds an attributes to the current active Element
     * @param key
     * @param value
     */
    public void addAttribute(String key, String value){
        contents.append(space);
        contents.append(key);
        contents.append(equals);
        contents.append(quotes);
        contents.append(value);
        contents.append(quotes);
    }

    /**
     * Ends the attributes for the currently active Element
     *
     */
    public void endAttributes(){
        contents.append(gt);
    }

    /**
     * Ends the specified element
     * @param ele String of element
     */
    public void endElement(String ele){
        appendEndElement(ele);
        contents.append(nextline);

    }

    /**
     * Returns XML String representation of what was appended/written on this
     */
    public String toString(){
        return contents.toString();
    }

    public static void main(String[] args) {
        XMLStringWriter x = new XMLStringWriter(4);

        x.beginXMLString("CODEBLOCKS");
        x.beginElement("PAGES", false);
        x.beginElement("PAGE", true);
        x.addAttribute("page-name", "ricarose");
        x.endAttributes();
        x.addDataElement("RICAROSE", "text");
        x.beginElement("Connectors", false);
        x.endElement("Connectors");
        x.endElement("PAGE");
        x.endElement("PAGES");
        x.endXMLString();

        System.out.println("produced xml: "+x);
    }

}
