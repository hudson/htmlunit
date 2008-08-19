/*
 * Copyright (c) 2002-2008 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.html;

import static com.gargoylesoftware.htmlunit.protocol.javascript.JavaScriptURLConnection.JAVASCRIPT_PREFIX;

import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Function;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.HTMLScriptElement;
import com.gargoylesoftware.htmlunit.xml.XmlPage;

/**
 * Wrapper for the HTML element "script".<br>
 * When a script tag references an external script (with attribute src) it gets executed when the node
 * is added to the DOM tree. When the script code is nested, it gets executed when the text node
 * containing the script is added to the HtmlScript.<br>
 * The ScriptFilter feature of NekoHtml can't be used because it doesn't allow immediate access to the DOM
 * (i.e. <code>document.write("&lt;span id='mySpan'/>"); document.getElementById("mySpan").tagName;</code>
 * can't work with a filter).
 *
 * @version $Revision: 3191 $
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:cse@dynabean.de">Christian Sell</a>
 * @author Marc Guillemot
 * @author David K. Taylor
 * @author Ahmed Ashour
 * @author Daniel Gredler
 * @author Dmitri Zoubkov
 * @author Sudhan Moghe
 * @see <a href="http://www.w3.org/TR/2000/WD-DOM-Level-1-20000929/level-one-html.html#ID-81598695">DOM Level 1</a>
 * @see <a href="http://www.w3.org/TR/2003/REC-DOM-Level-2-HTML-20030109/html.html#ID-81598695">DOM Level 2</a>
 */
public class HtmlScript extends HtmlElement {

    private static final long serialVersionUID = 5736570536821513938L;

    /** The HTML tag represented by this element. */
    public static final String TAG_NAME = "script";

    /** Invalid source attribute which should be ignored (used by JS libraries like jQuery). */
    private static final String SLASH_SLASH_COLON = "//:";

    /** Not really used? */
    private static int EventHandlerId_;

    private final transient Log mainLog_ = LogFactory.getLog(getClass());

    /**
     * Create an instance of HtmlScript
     *
     * @param namespaceURI the URI that identifies an XML namespace
     * @param qualifiedName the qualified name of the element type to instantiate
     * @param page the HtmlPage that contains this element
     * @param attributes the initial attributes
     */
    HtmlScript(final String namespaceURI, final String qualifiedName, final SgmlPage page,
            final Map<String, DomAttr> attributes) {
        super(namespaceURI, qualifiedName, page, attributes);
    }

    /**
     * Returns the value of the attribute "charset". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "charset"
     * or an empty string if that attribute isn't defined.
     */
    public final String getCharsetAttribute() {
        return getAttributeValue("charset");
    }

    /**
     * Returns the value of the attribute "type". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "type"
     * or an empty string if that attribute isn't defined.
     */
    public final String getTypeAttribute() {
        return getAttributeValue("type");
    }

    /**
     * Returns the value of the attribute "language". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "language"
     * or an empty string if that attribute isn't defined.
     */
    public final String getLanguageAttribute() {
        return getAttributeValue("language");
    }

    /**
     * Returns the value of the attribute "src". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "src"
     * or an empty string if that attribute isn't defined.
     */
    public final String getSrcAttribute() {
        return getAttributeValue("src");
    }

    /**
     * Returns the value of the attribute "event".
     * @return the value of the attribute "event"
     */
    public final String getEventAttribute() {
        return getAttributeValue("event");
    }

    /**
     * Returns the value of the attribute "for".
     * @return the value of the attribute "for"
     */
    public final String getHtmlForAttribute() {
        return getAttributeValue("for");
    }

    /**
     * Returns the value of the attribute "defer". Refer to the
     * <a href='http://www.w3.org/TR/html401/'>HTML 4.01</a>
     * documentation for details on the use of this attribute.
     *
     * @return the value of the attribute "defer"
     * or an empty string if that attribute isn't defined.
     */
    public final String getDeferAttribute() {
        return getAttributeValue("defer");
    }

    /**
     * Returns <tt>true</tt> if this script is deferred.
     * @return <tt>true</tt> if this script is deferred
     */
    protected boolean isDeferred() {
        return getDeferAttribute() != ATTRIBUTE_NOT_DEFINED;
    }

    /**
     * If setting the <tt>src</tt> attribute, this method executes the new JavaScript if necessary
     * (behavior varies by browser version). {@inheritDoc}
     */
    @Override
    protected void setAttributeValue(final String namespaceURI, final String qualifiedName,
        final String attributeValue, final boolean cloning) {

        boolean execute = false;
        if (namespaceURI == null && "src".equals(qualifiedName) && !cloning) {
            final boolean ie = getPage().getWebClient().getBrowserVersion().isIE();
            if (ie || (getAttribute("src").length() == 0 && getFirstChild() == null)) {
                // Always execute if IE; if FF, only execute if the "src" attribute
                // was undefined and there was no inline code.
                execute = true;
            }
        }

        super.setAttributeValue(namespaceURI, qualifiedName, attributeValue, cloning);

        if (execute) {
            executeScriptIfNeeded(true);
        }
    }

    /**
     * Executes the <tt>onreadystatechange</tt> handler when simulating IE, as well as executing
     * the script itself, if necessary. {@inheritDoc}
     */
    @Override
    protected void onAllChildrenAddedToPage() {
        if (getOwnerDocument() instanceof XmlPage) {
            return;
        }
        if (mainLog_.isDebugEnabled()) {
            mainLog_.debug("Script node added: " + asXml());
        }
        final boolean ie = getPage().getWebClient().getBrowserVersion().isIE();
        final boolean pageFinishedLoading = (getPage().getReadyState() == READY_STATE_COMPLETE);
        if (!ie || pageFinishedLoading || !isDeferred()) {
            setReadyStateComplete();
            executeScriptIfNeeded(true);
        }
        super.onAllChildrenAddedToPage();
    }

    /**
     * Executes this script node as inline script if necessary and/or possible.
     *
     * @param executeIfDeferred if <tt>false</tt>, and we are emulating IE, and the <tt>defer</tt>
     * attribute is defined, the script is not executed
     */
    private void executeInlineScriptIfNeeded(final boolean executeIfDeferred) {
        if (!isExecutionNeeded()) {
            return;
        }

        final boolean ie = getPage().getWebClient().getBrowserVersion().isIE();
        if (!executeIfDeferred && isDeferred() && ie) {
            return;
        }

        final String src = getSrcAttribute();
        if (src != HtmlElement.ATTRIBUTE_NOT_DEFINED) {
            return;
        }

        final DomCharacterData textNode = (DomCharacterData) getFirstChild();
        final String forr = getHtmlForAttribute();
        String event = getEventAttribute();

        final String scriptCode;
        if (event != ATTRIBUTE_NOT_DEFINED && forr != ATTRIBUTE_NOT_DEFINED) {
            // The event name can be like "onload" or "onload()".
            if (event.endsWith("()")) {
                event = event.substring(0, event.length() - 2);
            }
            final String handler = forr + "." + event;
            final String functionName = "htmlunit_event_handler_JJLL" + EventHandlerId_;
            scriptCode = "function " + functionName + "()\n"
                + "{" + textNode.getData() + "}\n"
                + handler + "=" + functionName + ";";
        }
        else {
            scriptCode = textNode.getData();
        }

        final String url = getPage().getWebResponse().getUrl().toExternalForm();
        final int line1 = getStartLineNumber();
        final int line2 = getEndLineNumber();
        final int col1 = getStartColumnNumber();
        final int col2 = getEndColumnNumber();
        final String desc = "script in " + url + " from (" + line1 + ", " + col1 + ") to (" + line2 + ", " + col2 + ")";
        ((HtmlPage) getPage()).executeJavaScriptIfPossible(scriptCode, desc, line1);
    }

    /**
     * Executes this script node if necessary and/or possible.
     *
     * @param executeIfDeferred if <tt>false</tt>, and we are emulating IE, and the <tt>defer</tt>
     * attribute is defined, the script is not executed
     */
    void executeScriptIfNeeded(final boolean executeIfDeferred) {
        if (!isExecutionNeeded()) {
            return;
        }

        final BrowserVersion browser = getPage().getWebClient().getBrowserVersion();
        final boolean ie = browser.isIE();
        if (!executeIfDeferred && isDeferred() && ie) {
            return;
        }

        final String src = getSrcAttribute();
        if (src.equals(SLASH_SLASH_COLON)) {
            return;
        }

        if (src != ATTRIBUTE_NOT_DEFINED) {
            if (src.startsWith(JAVASCRIPT_PREFIX)) {
                // <script src="javascript:'[code]'"></script>
                if (!ie || browser.getBrowserVersionNumeric() != 7) {
                    String code = StringUtils.removeStart(src, JAVASCRIPT_PREFIX).trim();
                    final int len = code.length();
                    if (len > 2) {
                        if ((code.charAt(0) == '\'' && code.charAt(len - 1) == '\'')
                            || (code.charAt(0) == '"' && code.charAt(len - 1) == '"')) {
                            code = code.substring(1, len - 1);
                            if (mainLog_.isDebugEnabled()) {
                                mainLog_.debug("Executing JavaScript: " + code);
                            }
                            ((HtmlPage) getPage()).executeJavaScriptIfPossible(code, code, getStartLineNumber());
                        }
                    }
                }
            }
            else {
                // <script src="[url]"></script>
                if (mainLog_.isDebugEnabled()) {
                    mainLog_.debug("Loading external JavaScript: " + src);
                }
                ((HtmlPage) getPage()).loadExternalJavaScriptFile(src, getCharsetAttribute());
            }
        }
        else if (getFirstChild() != null) {
            // <script>[code]</script>
            executeInlineScriptIfNeeded(executeIfDeferred);
        }
    }

    /**
     * Indicates if script execution is necessary and/or possible.
     *
     * @return <code>true</code> if the script should be executed
     */
    private boolean isExecutionNeeded() {
        final SgmlPage page = getPage();

        // If JavaScript is disabled, we don't need to execute.
        if (!page.getWebClient().isJavaScriptEnabled()) {
            return false;
        }

        //If innerHTML or outerHTML is being parsed
        if (page instanceof HtmlPage && ((HtmlPage) page).isParsingHtmlSnippet()) {
            return false;
        }

        // If the script node is nested in an iframe, a noframes, or a noscript node, we don't need to execute.
        for (DomNode o = this; o != null; o = o.getParentNode()) {
            if (o instanceof HtmlInlineFrame || o instanceof HtmlNoFrames || o instanceof HtmlNoScript) {
                return false;
            }
        }

        // If the underlying page no longer owns its window, the client has moved on (possibly
        // because another script set window.location.href), and we don't need to execute.
        if (page.getEnclosingWindow() != null && page.getEnclosingWindow().getEnclosedPage() != page) {
            return false;
        }

        // If the script language is not JavaScript, we can't execute.
        if (!HtmlPage.isJavaScript(getTypeAttribute(), getLanguageAttribute())) {
            final String t = getTypeAttribute();
            final String l = getLanguageAttribute();
            if (mainLog_.isWarnEnabled()) {
                mainLog_.warn("Script is not JavaScript (type: " + t + ", language: " + l + "). Skipping execution.");
            }
            return false;
        }

        // If the script's root ancestor node is not the page, the the script is not a part of the page.
        // If it isn't yet part of the page, don't execute the script; it's probably just being cloned.
        DomNode root = this;
        while (root.getParentNode() != null) {
            root = root.getParentNode();
        }
        if (root != getPage()) {
            return false;
        }

        return true;
    }

    /**
     * Sets the <tt>readyState</tt> to {@link DomNode#READY_STATE_COMPLETE} and executes the
     * <tt>onreadystatechange</tt> handler when simulating IE. Note that script nodes go
     * straight to the {@link DomNode#READY_STATE_COMPLETE} state, skipping all previous states.
     */
    protected void setReadyStateComplete() {
        final boolean ie = getPage().getWebClient().getBrowserVersion().isIE();
        if (!ie) {
            return;
        }

        setReadyState(READY_STATE_COMPLETE);
        final HTMLScriptElement script = (HTMLScriptElement) getScriptObject();
        final Function handler = script.getOnReadyStateChangeHandler();
        if (handler != null) {
            ((HtmlPage) getPage()).executeJavaScriptFunctionIfPossible(handler, script, new Object[0], this);
        }
    }

    /**
     * @see com.gargoylesoftware.htmlunit.html.HtmlInput#asText()
     * @return an empty string as the content of script is not visible by itself
     */
    @Override
    public String asText() {
        return "";
    }

    /**
     * Indicates if a node without children should be written in expanded form as XML
     * (i.e. with closing tag rather than with "/&gt;")
     * @return <code>true</code> to make generated XML readable as HTML
     */
    @Override
    protected boolean isEmptyXmlTagExpanded() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void printChildrenAsXml(final String indent, final PrintWriter printWriter) {
        final DomCharacterData textNode = (DomCharacterData) getFirstChild();
        if (textNode != null) {
            printWriter.println("//<![CDATA[");
            printWriter.println(textNode.getData());
            printWriter.println("//]]>");
        }
    }
}