import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;
import java.io.*;

public class DOMEcho {
    // Output encoding and print writer for output
    static final String outputEncoding = "UTF-8";
    private PrintWriter out;
    private int indent = 0;
    private final String basicIndent = "  ";  // Use two spaces for indentation

    // Method to print common node attributes
    private void printlnCommon(Node n) {
        out.print(" nodeName=\"" + n.getNodeName() + "\"");
        String val = n.getNamespaceURI();
        if (val != null) {
            out.print(" uri=\"" + val + "\"");
        }
        val = n.getPrefix();
        if (val != null) {
            out.print(" pre=\"" + val + "\"");
        }
        val = n.getLocalName();
        if (val != null) {
            out.print(" local=\"" + val + "\"");
        }
        val = n.getNodeValue();
        if (val != null) {
            out.print(" nodeValue=");
            if (val.trim().equals("")) {  // Handle whitespace nodes
                out.print("[WS]");
            } else {
                out.print("\"" + n.getNodeValue() + "\"");
            }
        }
        out.println();
    }

    // Method to handle indentation
    private void outputIndentation() {
        for (int i = 0; i < indent; i++) {
            out.print(basicIndent);
        }
    }

    // Recursive method to print out the DOM tree
    private void echo(Node n) {
        outputIndentation();
        int type = n.getNodeType();
        switch (type) {
            case Node.ATTRIBUTE_NODE:
                out.print("ATTR:");
                printlnCommon(n);
                break;
            case Node.CDATA_SECTION_NODE:
                out.print("CDATA:");
                printlnCommon(n);
                break;
            case Node.COMMENT_NODE:
                out.print("COMM:");
                printlnCommon(n);
                break;
            case Node.ELEMENT_NODE:
                out.print("ELEM:");
                printlnCommon(n);
                // Print attributes of the element
                NamedNodeMap atts = n.getAttributes();
                indent++;
                for (int i = 0; i < atts.getLength(); i++) {
                    Node att = atts.item(i);
                    echo(att);
                }
                indent--;
                break;
            case Node.TEXT_NODE:
                out.print("TEXT:");
                printlnCommon(n);
                break;
            default:
                out.print("Unsupported node type: " + type);
                printlnCommon(n);
                break;
        }

        // Recurse through children if any
        indent++;
        for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling()) {
            echo(child);
        }
        indent--;
    }

    // Constructor
    DOMEcho(PrintWriter out) {
        this.out = out;
    }

    // Main entry point
    public static void main(String[] args) throws Exception {
        String filename = null;

        // Parsing command-line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-usage") || args[i].equals("-help")) {
                usage();
                return;
            } else {
                filename = args[i];  // The last argument is the XML filename
            }
        }

        if (filename == null) {
            usage();
            return;
        }

        // Step 1: Create a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);  // Required for XML namespace support
        dbf.setValidating(false);  // Disable validation for simplicity (can be enabled if needed)

        // Step 2: Create a DocumentBuilder
        DocumentBuilder db = dbf.newDocumentBuilder();
        db.setErrorHandler(new MyErrorHandler(new PrintWriter(System.err, true)));

        // Step 3: Parse the XML file into a DOM object
        Document doc = db.parse(new File(filename));

        // Step 4: Print out the DOM tree
        new DOMEcho(new PrintWriter(System.out, true)).echo(doc);
    }

    // Method to print usage information
    private static void usage() {
        System.err.println("Usage: DOMEcho <file.xml>");
        System.err.println("  -usage or -help = this message");
        System.exit(1);
    }

    // Custom error handler for SAX parsing errors
    private static class MyErrorHandler implements ErrorHandler {
        private PrintWriter out;

        MyErrorHandler(PrintWriter out) {
            this.out = out;
        }

        // Get detailed information from the SAXParseException
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            return "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();
        }

        // Handle warning messages
        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        // Handle error messages
        public void error(SAXParseException spe) throws SAXException {
            out.println("Error: " + getParseExceptionInfo(spe));
        }

        // Handle fatal errors
        public void fatalError(SAXParseException spe) throws SAXException {
            out.println("Fatal Error: " + getParseExceptionInfo(spe));
        }
    }
}