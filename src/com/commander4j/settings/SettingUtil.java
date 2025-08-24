package com.commander4j.settings;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SettingUtil
{

	public Settings readConfigFromXml()
	{
		Settings result = new Settings();

		try
		{

			File xmlFile = new File("./xml/config/config.xml");

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			// Parse the XML
			Document document = builder.parse(xmlFile);
			document.getDocumentElement().normalize();

			// Get the <settings> element
			NodeList settingsList = document.getElementsByTagName("settings");

			if (settingsList.getLength() > 0)
			{
				Element settings = (Element) settingsList.item(0);

				// Loop over child nodes
				NodeList children = settings.getChildNodes();
				for (int i2 = 0; i2 < children.getLength(); i2++)
				{
					Node child = children.item(i2);

					if (child.getNodeType() == Node.ELEMENT_NODE)
					{
						String name = child.getNodeName();
						switch (name)
						{
							case "inputFolder":
							{
								result.defaultInputFolder = child.getTextContent().trim();
								break;
							}
							case "labelOrder":
							{
								result.labelOrder = child.getTextContent().trim();
								break;
							}
							case "portNo":
							{
								result.portNumber = child.getTextContent().trim();
								break;
							}
							case "magnification":
							{
								result.magnification = child.getTextContent().trim();
	
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public boolean saveConfigToXml(Settings settings)
	{

		boolean result = false;

		try
		{

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// Root element
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("config");
			Element settingsElement = doc.createElement("settings");

			rootElement.appendChild(settingsElement);

			doc.appendChild(rootElement);

			Element inputFolder = (Element) doc.createElement("inputFolder");
			inputFolder.appendChild(doc.createTextNode(settings.defaultInputFolder));
			settingsElement.appendChild(inputFolder);

			Element labelOrder = (Element) doc.createElement("labelOrder");
			labelOrder.appendChild(doc.createTextNode(settings.labelOrder));
			settingsElement.appendChild(labelOrder);

			Element portNo = (Element) doc.createElement("portNo");
			portNo.appendChild(doc.createTextNode(settings.portNumber));
			settingsElement.appendChild(portNo);

			Element magnification = (Element) doc.createElement("magnification");
			magnification.appendChild(doc.createTextNode(settings.magnification));
			settingsElement.appendChild(magnification);

			// Write the content into XML file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Pretty print
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DOMSource source = new DOMSource(doc);
			StreamResult streamResult = new StreamResult(new File("./xml/config/config.xml"));

			transformer.transform(source, streamResult);

			result = true;
		}
		catch (Exception ex)
		{

		}

		return result;

	}

}
