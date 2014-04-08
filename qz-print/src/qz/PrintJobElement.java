/**
 * @author Tres Finocchiaro
 *
 * Copyright (C) 2013 Tres Finocchiaro, QZ Industries
 *
 * IMPORTANT: This software is dual-licensed
 *
 * LGPL 2.1 This is free software. This software and source code are released
 * under the "LGPL 2.1 License". A copy of this license should be distributed
 * with this software. http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * QZ INDUSTRIES SOURCE CODE LICENSE This software and source code *may* instead
 * be distributed under the "QZ Industries Source Code License", available by
 * request ONLY. If source code for this project is to be made proprietary for
 * an individual and/or a commercial entity, written permission via a copy of
 * the "QZ Industries Source Code License" must be obtained first. If you've
 * obtained a copy of the proprietary license, the terms and conditions of the
 * license apply only to the licensee identified in the agreement. Only THEN may
 * the LGPL 2.1 license be voided.
 *
 */
package qz;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.swing.JEditorPane;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageable;
import qz.exception.InvalidRawImageException;
import qz.exception.NullCommandException;

/**
 * A PrintJobElement is a piece of a PrintJob that contains a data string,
 * format and sequence number for ordering.
 * @author Thomas Hart
 */
public class PrintJobElement {
    
    private final PrintJobElementType type;
    private final PrintJob pj;
    private final ByteArrayBuilder data;

    private int sequence;
    private boolean prepared;
    private ByteArrayBuilder preparedData;
    private final Charset charset;
    private int imageX = 0;
    private int imageY = 0;
    private int dotDensity = 32;
    private LanguageType lang;
    private String xmlTag;
    private BufferedImage bufferedImage;
    private PDDocument pdfFile;
    private PDPageable pdfPages;
    private String pdfFileName;
    private JEditorPane rtfEditor;
    
    PrintJobElement(PrintJob pj, ByteArrayBuilder data, PrintJobElementType type, Charset charset, String lang, int dotDensity) {
        
        this.lang = LanguageType.getType(lang);
        this.dotDensity = dotDensity;
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
        
    }

    PrintJobElement(PrintJob pj, ByteArrayBuilder data, PrintJobElementType type, Charset charset, String lang, int imageX, int imageY) {
        
        this.lang = LanguageType.getType(lang);
        this.imageX = imageX;
        this.imageY = imageY;
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
        
    }
    PrintJobElement(PrintJob pj, ByteArrayBuilder data, PrintJobElementType type, Charset charset, String xmlTag) {
        
        this.xmlTag = xmlTag;
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
        
    }

    PrintJobElement(PrintJob pj, ByteArrayBuilder data, PrintJobElementType type, Charset charset) {
        
        this.pj = pj;
        this.data = data;
        this.type = type;
        this.charset = charset;
        
        prepared = false;
    }
    
    /**
     * Prepare the PrintJobElement
     * 
     * @throws IOException
     * @throws InvalidRawImageException
     * @throws NullCommandException 
     */
    public void prepare() throws IOException, InvalidRawImageException, NullCommandException {
        PrintJobElementPreparer preparer = new PrintJobElementPreparer(type, data, charset, this);
        Thread preparerThread = new Thread(preparer);
        preparerThread.start();
    }
    
    /**
     * Callback for when the element is done preparing
     * 
     * @param preparedData The returned data
     * @param bufferedImage The returned image
     * @param rtfEditor The returned rtf editor JEditorPane
     * @param pdfFile The PDDocument representing the PDF file
     */
    public void donePreparing(ByteArrayBuilder preparedData, BufferedImage bufferedImage, JEditorPane rtfEditor, PDDocument pdfFile) {
        this.prepared = true;
        this.preparedData = preparedData;
        this.bufferedImage = bufferedImage;
        this.rtfEditor = rtfEditor;
        this.pdfFile = pdfFile;
        LogIt.log("Done preparing PrintJobElement.");
    }
    
    /**
     * Check if the PrintJobElement has been prepared
     * 
     * @return Boolean representing whether the PrintJobElement is prepared
     */
    public boolean isPrepared() {
        return prepared;
    }
    
    /**
     * Get the PrintJobElement's data. In the case of a raw element this is raw 
     * data, though other element types will use this to hold a url
     * 
     * @return The element's data
     */
    public ByteArrayBuilder getData() {
        return preparedData;
    }
    
    /**
     * Getter for the bufferedImage. This is used for PostScript image files
     * 
     * @return The buffered image data
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }
    
    /**
     * Getter for the PrintJobElement's charset
     * 
     * @return The element's charset
     */
    public Charset getCharset() {
        return charset;
    }
    
    /**
     * Getter for the PrintJobElement's type
     * 
     * @return The element's type
     */
    public PrintJobElementType getType() {
        return type;
    }
    
    /**
     * Getter for the PDDocument object. This object is used for pdf PrintJobElements
     * 
     * @return The processed PDDocument
     */
    public PDDocument getPDFFile() {
        return pdfFile;
    }
    
    /**
     * This function controls PDF rendering to a graphics context that can be
     * used for printing
     * 
     * @param graphics The target graphics context
     * @param pageFormat The desired pageFormat
     * @param pageIndex The page index currently being processed
     * @param leftMargin The left margin to use when printing
     * @param topMargin The top margin to use when printing
     * 
     * @return PAGE_EXISTS on success
     * @throws PrinterException
     */
    /*
    public int printPDFRenderer(Graphics graphics, PageFormat pageFormat, int pageIndex, int leftMargin, int topMargin) throws PrinterException {
        
        if (pdfPages == null) {
            throw new PrinterException("No PDF data loaded.");
        }
        
        return pdfPages.print(graphics, pageFormat, pageIndex);
        
    }
    */
    
    /**
     * This function returns the RTF data to the print job for printing
     * @return The JEditorPane containing the rendered RTF data
     */
    public JEditorPane getRtfData() {
        return rtfEditor;
    }
    
    /**
     * This function returns the RTF data height to the print job for printing
     * @return The height of the RTF pane
     */
    public int getRtfHeight() {
        return rtfEditor.getHeight();
    }
    
    /**
     * This function returns the rtf data width to the print job for printing
     * @return The width of the RTF pane
     */
    public int getRtfWidth() {
        return rtfEditor.getWidth();
    }
    
    /**
     * This function returns the LanguageType associated with this element
     * @return The LanguageType associated with this element
     */
    public LanguageType getLang() {
        return lang;
    }
    
    /**
     * This function returns the dot density associated with this element
     * @return The dot density associated with this element
     */
    public int getDotDensity() {
        return dotDensity;
    }
    
    /**
     * This function returns the image x position associated with this element
     * @return The image x position associated with this element
     */
    public int getImageX() {
        return imageX;
    }
    
    /**
     * This function returns the image y position associated with this element
     * @return The image y position associated with this element
     */
    public int getImageY() {
        return imageY;
    }
    
    /**
     * This function returns the XML tag associated with this element
     * @return The XML tag associated with this element
     */
    public String getXmlTag() {
        return xmlTag;
    }
}
