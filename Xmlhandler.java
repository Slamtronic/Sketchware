package com.slamtronic.storor;


    

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;

public class Xmlhandler {
    boolean done=false;
    private Document doc;
    private String filePath;
   DocumentBuilder dBuilder;
   Element newRootElement;
   
public Xmlhandler(String filePath,boolean bui) {
        this.filePath = filePath;
        try {
            File xmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
   dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
        
        } catch (Exception e) {
            System.out.println("Issue with XML file: " + e);
        }
        if(bui){ saveXML();}
    }

    // Method to get the path of the XML file
    public String getXmlFilePath() {
        return this.filePath;
    }

    // Method to get all parent element names
    public List<String> listParentNames() {
        List<String> parentNames = new ArrayList<>();
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                parentNames.add(node.getNodeName());
            }
        }
        return parentNames;
    }

    // Method to get all attributes of a parent element
    public Map<String, Object> getElementAttributes(Element parent) {
        Map<String, Object> attributesMap = new HashMap<>();
        
            for (int i = 0; i < parent.getAttributes().getLength(); i++) {
                String attrName = parent.getAttributes().item(i).getNodeName();
                String attrValue = parent.getAttributes().item(i).getNodeValue();
                attributesMap.put(attrName, attrValue);
            }
        
        return attributesMap;
    }

    // Method to get child element names of a parent element
    public List<String> getChildElementNames(String parentName) {
        List<String> childNames = new ArrayList<>();
        NodeList parentNodes = doc.getElementsByTagName(parentName);
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            NodeList childNodes = parentElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    childNames.add(childNode.getNodeName());
                }
            }
        }
        return childNames;
    }

//##############################
    // Method to create and return a child element by name
    public Element createChildElement(String parentName, String childName) {
        NodeList parentNodes = doc.getElementsByTagName(parentName);
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            Element childElement = doc.createElement(childName);
            parentElement.appendChild(childElement);
            return childElement;
        }
        return null;
    }
    
    //##############################

    // Method to get a specific child element by name
    public Element getChildElement(String parentName, String childName) {
        NodeList parentNodes = doc.getElementsByTagName(parentName);
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            NodeList childNodes = parentElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(childName)) {
                    return (Element) childNode;
                }
            }
        }
        return null;
    }

    // Method to add attributes to an element
    public void addAttributesToElement(String elementName, Map<String, String> attributes) {
        NodeList nodes = doc.getElementsByTagName(elementName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                element.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    // Method to delete an element by name
    public void deleteElement(String elementName) {
        NodeList nodes = doc.getElementsByTagName(elementName);
        if (nodes.getLength() > 0) {
            Node nodeToDelete = nodes.item(0);
            nodeToDelete.getParentNode().removeChild(nodeToDelete);
        }
    }

    // Method to delete a specific child element by name
    public void deleteChildElement(String parentName, String childName) {
        NodeList parentNodes = doc.getElementsByTagName(parentName);
        if (parentNodes.getLength() > 0) {
            Element parentElement = (Element) parentNodes.item(0);
            NodeList childNodes = parentElement.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(childName)) {
                    parentElement.removeChild(childNode);
                    break;
                }
            }
        }
    }

    // Method to delete an attribute from an element
    public void deleteAttribute(String elementName, String attributeName) {
        NodeList nodes = doc.getElementsByTagName(elementName);
        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            element.removeAttribute(attributeName);
        }
    }

    
 //////////////////////////////////////////////////////////////////////////

 
 public Element newRoot(String str,boolean neew){

 if(neew){ 
newRootElement=null;
doc = dBuilder.newDocument();
   try{ 
 newRootElement= doc.createElement(str); 
doc.appendChild(newRootElement);}
catch(Exception e){}
saveXML();
return newRootElement;}
else{ return newRootElement; }

   }


public Element getElementByName(List<Element> elements, String tagName) {
        for (Element element : elements) {
            if (element.getNodeName().equals(tagName)) {
                return element; // Return the matching element
            }
        }
        return null; // Return null if no matching element is found
    }
 
 
 
 public Element getParent(Element element){
     
    Node parentNode = element.getParentNode();
        if (parentNode instanceof Element) {
            Element parent = (Element) parentNode; 
            return parent;
        } else {
            return null;
        }
 }
 
 
 
public  boolean isElementInList(List<Element> elements, String targetElement) {
        for (Element currentElement : elements) {
            if (currentElement.getNodeName().equals(targetElement)) {
                return true; // Target element found in the list
            }
        }
        return false; // Target element not found
    }
 
 
 
protected Element getChildElementByName(Element parent, String tagName) {
        NodeList children = parent.getElementsByTagName(tagName);
        if (children.getLength() > 0) {
            return (Element) children.item(0); // Return the first matching child element
        }
        return null; // Return null if no matching child is found
    }
 
 
public List<Element> getRootElements() {
        List<Element> rootList = new ArrayList<>();
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {Element nodeElem=(Element)node;
                rootList.add(nodeElem);
            }
        }
        return rootList;
    }
    
    
public List<Element> getChildElements(Element parent){
        List<Element> childElements = new ArrayList<>();
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) { 
                Node childNode = childNodes.item(i);
        if( childNode.getNodeType() == Node.ELEMENT_NODE){        Element newE =(Element) childNode;
                childElements.add(newE);}
                 
                }return childElements;
    }
    
    
public Element createElement(Element parent, String childName) {
    Element childElement = doc.createElement(childName);
            parent.appendChild(childElement);
         saveXML();
return childElement;
              }
              
protected  void addAttributeToElement(Element elem, String key, String value)  {try{ 
  elem.setAttribute(key, value);}
  catch(Exception e){}
        saveXML();
    }
    
    
   public void deleteChildElement(Element element) {
           Node parentNode = element.getParentNode(); // Get its parent
                parentNode.removeChild(element); 
    saveXML();    }


// Get attribute value
public String getAttributeValue(Element element, String attributeName) {
    return element.getAttribute(attributeName); // Retrieve the attribute value
}

// Set attribute value
public void setAttributeValue(Element element, String attributeName, String value) {
    element.setAttribute(attributeName, value); // Set/update the attribute value
}

    
    
              
public void saveXML() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);
            done=true;
        } catch (TransformerConfigurationException e) {done=false;
            e.printStackTrace(); // Handle TransformerConfigurationException
        } catch (TransformerException e) {done=false;
            e.printStackTrace(); // Handle TransformerException
        }
    }
    
}
