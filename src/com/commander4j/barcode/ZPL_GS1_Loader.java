package com.commander4j.barcode;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.commander4j.memory.ZPLMemory;

public class ZPL_GS1_Loader {

    public void loadGS1Data(ZPLMemory memory,File xmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList eanNodes = doc.getElementsByTagName("ean");

            for (int i = 0; i < eanNodes.getLength(); i++) {
                Node node = eanNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) node;

                    String appId = eElement.getAttribute("app_id");
                    String description = eElement.getAttribute("description");
                    int maxLength = Integer.parseInt(eElement.getAttribute("max_length"));
                    String fixedVariable = eElement.getAttribute("fixed_variable");

                    ZPL_GS1_AppData data = new ZPL_GS1_AppData();
                    data.description = description;
                    data.max_length = maxLength;
                    data.fixed_variable = fixedVariable;

                    memory.app_id_data.put(appId, data);
                }
            }

            System.out.println("Loaded " + memory.app_id_data.size() + " GS1 Application Identifiers.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}