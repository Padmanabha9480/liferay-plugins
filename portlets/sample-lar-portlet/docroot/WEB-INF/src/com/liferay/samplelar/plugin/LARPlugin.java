/**
 * Copyright (c) 2000-2009 Liferay, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.liferay.samplelar.plugin;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.MapUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.zip.ZipReader;
import com.liferay.portal.kernel.zip.ZipWriter;
import com.liferay.portal.lar.PortletDataContext;
import com.liferay.portal.lar.PortletDataException;
import com.liferay.portal.lar.PortletDataHandler;
import com.liferay.portal.lar.PortletDataHandlerBoolean;
import com.liferay.portal.lar.PortletDataHandlerChoice;
import com.liferay.portal.lar.PortletDataHandlerControl;

import java.util.Date;
import java.util.Map;

import javax.portlet.PortletPreferences;

/**
 * <a href="LARPlugin.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Aug�
 *
 */
public class LARPlugin implements PortletDataHandler {

	public PortletPreferences deleteData(
			PortletDataContext context, String portletId,
			PortletPreferences prefs)
		throws PortletDataException {

		return null;
	}

	public String exportData(
			PortletDataContext context, String portletId,
			PortletPreferences prefs)
		throws PortletDataException {

		Map parameterMap = context.getParameterMap();

		boolean exportData = MapUtil.getBoolean(
			parameterMap, "export-sample-lar-portlet-data",
			_enableExport.getDefaultState());

		if (_log.isDebugEnabled()) {
			if (exportData) {
				_log.debug("Exporting data is enabled");
			}
			else {
				_log.debug("Exporting data is disabled");
			}
		}

		if (!exportData) {
			return null;
		}

		try {
			long exportDate = System.currentTimeMillis();

			if (_log.isInfoEnabled()) {
				_log.info("Exporting LAR on " + new Date(exportDate));
			}

			prefs.setValue("last-export-date", String.valueOf(exportDate));

			prefs.store();

			String data = "<data-file />";

			ZipWriter zipWriter = context.getZipWriter();

			if (zipWriter != null) {
				boolean createReadMe = MapUtil.getBoolean(
					parameterMap, "create-readme",
					_createReadme.getDefaultState());

				if (createReadMe) {
					if (_log.isInfoEnabled()) {
						_log.info("Writing to zip");
					}

					zipWriter.addEntry(
						portletId + "/README.txt", "Test writing to zip.");
				}

				String dataType = MapUtil.getString(
					parameterMap, "data-type", _dataType.getDefaultChoice());

				if (Validator.equals(dataType, "csv")) {
					StringBuilder csv = new StringBuilder();

					csv.append("data 1," + new Date() + "\n");
					csv.append("data 2," + new Date() + "\n");

					String filePath = portletId + "/data.csv";

					data = "<data-file>" + filePath + "</data-file>";

					zipWriter.addEntry(filePath, csv.toString());
				}
				else if (Validator.equals(dataType, "xml")) {
					StringBuilder xml = new StringBuilder();

					xml.append("<?xml version=\"1.0\"?>\n\n");
					xml.append("<records>\n");
					xml.append("\t<record>\n");
					xml.append("\t\t<field>data 1</field>\n");
					xml.append("\t\t<field>" + new Date() + "</field>\n");
					xml.append("\t</record>\n");
					xml.append("\t<record>\n");
					xml.append("\t\t<field>data 2</field>\n");
					xml.append("\t\t<field>" + new Date() + "</field>\n");
					xml.append("\t</record>\n");
					xml.append("</records>");

					String filePath = portletId + "/data.xml";

					data = "<data-file>" + filePath + "</data-file>";

					zipWriter.addEntry(filePath, xml.toString());
				}
			}

			return data;
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}
	}

	public PortletDataHandlerControl[] getExportControls()
		throws PortletDataException {

		return new PortletDataHandlerControl[] {_enableExport};
	}

	public PortletDataHandlerControl[] getImportControls()
		throws PortletDataException{

		return new PortletDataHandlerControl[] {_enableImport};
	}

	public PortletPreferences importData(PortletDataContext context,
			String portletId, PortletPreferences prefs, String data)
			throws PortletDataException {

		Map parameterMap = context.getParameterMap();

		boolean importData = MapUtil.getBoolean(
			parameterMap, "import-sample-lar-portlet-data",
			_enableImport.getDefaultState());

		if (_log.isDebugEnabled()) {
			if (importData) {
				_log.debug("Importing data is enabled");
			}
			else {
				_log.debug("Importing data is disabled");
			}
		}

		if (!importData) {
			return null;
		}

		try {
			long importDate = System.currentTimeMillis();

			prefs.setValue("last-import-date", String.valueOf(importDate));

			if (_log.isInfoEnabled()) {
				_log.info("Importing data " + data);
			}

			ZipReader zipReader = context.getZipReader();

			if (zipReader != null) {
				_log.info(
					"From README file:\n\n" +
						zipReader.getEntryAsString(portletId + "/README.txt"));
			}

			return prefs;
		}
		catch (Exception e) {
			throw new PortletDataException(e);
		}
	}

	public boolean isPublishToLiveByDefault() {
		return true;
	}

	private static final String _NAMESPACE = "lar-plugin";

	private static final PortletDataHandlerBoolean _createReadme =
		new PortletDataHandlerBoolean(_NAMESPACE, "create-readme", true, true);

	private static final PortletDataHandlerChoice _dataType =
		new PortletDataHandlerChoice(
			_NAMESPACE, "data-type", 1, new String[] {"csv", "xml"});

	private static final PortletDataHandlerBoolean _enableExport =
		new PortletDataHandlerBoolean(
			_NAMESPACE, "export-sample-lar-portlet-data", true,
			new PortletDataHandlerControl[] {_createReadme, _dataType});

	private static final PortletDataHandlerBoolean _enableImport =
		new PortletDataHandlerBoolean(
			_NAMESPACE, "import-sample-lar-portlet-data", true, true);

	private static Log _log = LogFactoryUtil.getLog(LARPlugin.class);

}