/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007 - 2010 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.impl.service.webadmin;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.db.PluginConfiguration;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.pa.PluginInfo.ConfigurationType;
import eu.sqooss.service.util.StringUtils;

public class PluginsView extends AbstractView{

    public PluginsView(BundleContext bundlecontext, VelocityContext vc) {
        super(bundlecontext, vc);
    }

    /**
     * Renders the various plug-in's views.
     * 
     * @param req the servlet's request object
     * 
     * @return The HTML presentation of the generated view.
     */
    public String render(HttpServletRequest req) {
        // Stores the assembled HTML content
        StringBuilder b = new StringBuilder("\n");
        // Stores the accumulated error messages
        StringBuilder e = new StringBuilder();
        // Indentation spacer
        long in = 6;

        // Request parameters
        String reqParAction        = "action";
        String reqParHashcode      = "pluginHashcode";
        String reqParPropName      = "propertyName";
        String reqParPropDescr     = "propertyDescription";
        String reqParPropType      = "propertyType";
        String reqParPropValue     = "propertyValue";
        String reqParShowProp      = "showProperties";
        String reqParShowActv      = "showActivators";
        // Recognized "action" parameter's values
        String actValInstall       = "installPlugin";
        String actValUninstall     = "uninstallPlugin";
        String actValSync          = "syncPlugin";
        String actValReqAddProp    = "createProperty";
        String actValReqUpdProp    = "updateProperty";
        String actValConAddProp    = "confirmProperty";
        String actValConRemProp    = "removeProperty";
        // Request values
        String reqValAction        = "";
        String reqValHashcode      = null;
        String reqValPropName      = null;
        String reqValPropDescr     = null;
        String reqValPropType      = null;
        String reqValPropValue     = null;
        boolean reqValShowProp     = false;         // Show plug-in properties
        boolean reqValShowActv     = false;         // Show plug-in activators
        // Info object of the selected plug-in
        PluginInfo selPI           = null;

        // Proceed only when at least one plug-in is registered
        if (sobjPA.listPlugins().isEmpty()) {
            noPlugins(b, in);
        }
        else {
            // ===============================================================
            // Parse the servlet's request object
            // ===============================================================
            if (req != null) {
                // DEBUG: Dump the servlet's request parameter
                if (DEBUG) {
                    b.append(debugRequest(req));
                }

                // Retrieve the selected editor's action (if any)
                reqValAction = req.getParameter(reqParAction);
                if (reqValAction == null) {
                    reqValAction = "";
                };
                // Retrieve the various display flags
                if ((req.getParameter(reqParShowProp) != null)
                        && (req.getParameter(reqParShowProp).equals("true"))) {
                    reqValShowProp = true;
                }
                if ((req.getParameter(reqParShowActv) != null)
                        && (req.getParameter(reqParShowActv).equals("true"))) {
                    reqValShowActv = true;
                }
                // Retrieve the selected configuration property's values
                if ((reqValAction.equals(actValConAddProp))
                        || (reqValAction.equals(actValReqUpdProp))
                        || (reqValAction.equals(actValConRemProp))) {
                    // Name, description, type and value
                    reqValPropName  = req.getParameter(reqParPropName);
                    reqValPropDescr = req.getParameter(reqParPropDescr);
                    reqValPropType  = req.getParameter(reqParPropType);
                    reqValPropValue = req.getParameter(reqParPropValue);
                }
                // Retrieve the selected plug-in's hash code
                reqValHashcode = req.getParameter(reqParHashcode);
                // Plug-in based actions
                if (reqValHashcode != null) {
                    // =======================================================
                    // Plug-in install request
                    // =======================================================
                    if (reqValAction.equals(actValInstall)) {
                        pluginInstallRequest(e, reqValHashcode);
                    }
                    // =======================================================
                    // Plug-in un-install request
                    // =======================================================
                    else if (reqValAction.equals(actValUninstall)) {
                        pluginUninstallRequest(e, reqValHashcode);
                    } 
                }
                // Retrieve the selected plug-in's info object
                if (reqValHashcode != null) {
                    selPI = sobjPA.getPluginInfo(reqValHashcode);
                }
                // Plug-in info based actions
                if ((selPI != null) && (selPI.installed)) {
                    // =======================================================
                    // Plug-in synchronize (on all projects) request
                    // =======================================================
                    if (reqValAction.equals(actValSync)) {
                        compMA.syncMetrics(sobjPA.getPlugin(selPI));
                    }
                    // =======================================================
                    // Plug-in's configuration property removal
                    // =======================================================
                    else if (reqValAction.equals(actValConRemProp)) {
                        selPI = pluginConfigPropertyRemoval(e, reqValHashcode,
								reqValPropName, reqValPropType, selPI);
                        // Return to the update view upon error
                        if (e.toString().length() > 0) {
                            reqValAction = actValReqUpdProp;
                        }
                    }
                    // =======================================================
                    // Plug-in's configuration property creation/update
                    // =======================================================
                    else if (reqValAction.equals(actValConAddProp)) {
                        // Check for a property update
                        boolean update = selPI.hasConfProp(
                                reqValPropName, reqValPropType);
                        // Update configuration property
                        selPI = pluginConfigurationUpdateCreateProperty(e,
								reqValHashcode, reqValPropName,
								reqValPropDescr, reqValPropType,
								reqValPropValue, selPI, update);
                        // Return to the create/update view upon error
                        if (e.toString().length() > 0) {
                            if (update) reqValAction = actValReqUpdProp;
                            else reqValAction = actValReqAddProp;
                        }
                    }
                }
            }

            // ===============================================================
            // Create the form
            // ===============================================================
            b.append(sp(in++) + "<form id=\"metrics\""
                    + " name=\"metrics\""
                    + " method=\"post\""
                    + " action=\"/index\">\n");

            // ===============================================================
            // Display the accumulated error messages (if any)
            // ===============================================================
            b.append(errorFieldset(e, in));

            // ===============================================================
            // "Create/update configuration property" editor
            // ===============================================================
            if ((selPI != null) && (selPI.installed)
                    && ((reqValAction.equals(actValReqAddProp))
                            || (reqValAction.equals(actValReqUpdProp)))) {
                createUpdateConfigurationProperty(b, in, reqParAction,
						reqParPropName, reqParPropDescr, reqParPropType,
						reqParPropValue, actValConAddProp, actValConRemProp,
						reqValPropName, reqValPropDescr, reqValPropType,
						reqValPropValue, selPI);
            }
            // ===============================================================
            // Plug-in editor
            // ===============================================================
            else if (selPI != null) {
                in = pluginEditForm(b, in, reqParAction, reqParHashcode,
						reqParPropName, reqParPropDescr, reqParPropType,
						reqParPropValue, actValInstall, actValUninstall,
						actValSync, actValReqAddProp, actValReqUpdProp, selPI);
            }
            // ===============================================================
            // Plug-ins list
            // ===============================================================
            else {
                in = pluginList(b, in, reqParHashcode, reqParShowProp,
						reqParShowActv, reqValShowProp, reqValShowActv);
            }

            // ===============================================================
            // INPUT FIELDS
            // ===============================================================
            // "Action type" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParAction + "\""
                    + " name=\"" + reqParAction + "\""
                    + " value=\"\">\n");
            // "Selected plug-in's hash code" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParHashcode + "\""
                    + " name=\"" + reqParHashcode + "\""
                    + " value=\""
                    + ((reqValHashcode != null) ? reqValHashcode : "")
                    + "\">\n");
            // "Configuration attribute's name" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParPropName + "\""
                    + " name=\"" + reqParPropName + "\""
                    + " value=\""
                    + ((reqValPropName != null) ? reqValPropName : "")
                    + "\">\n");
            // "Configuration attribute's description" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParPropDescr + "\""
                    + " name=\"" + reqParPropDescr + "\""
                    + " value=\""
                    + ((reqValPropDescr != null) ? reqValPropDescr : "")
                    + "\">\n");
            // "Configuration attribute's type" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParPropType + "\""
                    + " name=\"" + reqParPropType + "\""
                    + " value=\""
                    + ((reqValPropType != null) ? reqValPropType : "")
                    + "\">\n");
            // "Configuration attribute's value" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParPropValue + "\""
                    + " name=\"" + reqParPropValue + "\""
                    + " value=\""
                    + ((reqValPropValue != null) ? reqValPropValue : "")
                    + "\">\n");
            // "Show configuration properties" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParShowProp + "\""
                    + " name=\"" + reqParShowProp + "\""
                    + " value=\""
                    + reqValShowProp
                    + "\">\n");
            // "Show activators" input field
            b.append(sp(in) + "<input type=\"hidden\""
                    + " id=\"" + reqParShowActv + "\""
                    + " name=\"" + reqParShowActv + "\""
                    + " value=\""
                    + reqValShowActv
                    + "\">\n");

            // ===============================================================
            // Close the form
            // ===============================================================
            b.append(sp(--in) + "</form>\n");
        }

        return b.toString();
    }

	/**
	 * @param b
	 * @param in
	 * @param reqParHashcode
	 * @param reqParShowProp
	 * @param reqParShowActv
	 * @param reqValShowProp
	 * @param reqValShowActv
	 * @return
	 */
	private long pluginList(StringBuilder b, long in, String reqParHashcode,
			String reqParShowProp, String reqParShowActv,
			boolean reqValShowProp, boolean reqValShowActv) {
		// Create the field-set
		b.append(sp(in) + "<fieldset>\n");
		b.append(sp(++in) + "<legend>All plug-ins</legend>\n");
		// Retrieve information for all registered metric plug-ins
		Collection<PluginInfo> l = sobjPA.listPlugins();
		//------------------------------------------------------------
		// Create the header row
		//------------------------------------------------------------
		b.append(sp(in) + "<table>\n");
		b.append(sp(++in) + "<thead>\n");
		b.append(sp(++in) + "<tr class=\"head\">\n");
		b.append(sp(++in) + "<td class=\"head\""
		        + " style=\"width: 80px;\">"
		        + "Status</td>\n");
		b.append(sp(in) + "<td class=\"head\""
		        + " style=\"width: 30%;\">"
		        + "Name</td>\n");
		b.append(sp(in) + "<td class=\"head\""
		        + " style=\"width: 40%;\">"
		        + "Class</td>\n");
		b.append(sp(in) + "<td class=\"head\">Version</td>\n");
		b.append(sp(--in) + "</tr>\n");
		b.append(sp(--in) + "</thead>\n");
		//------------------------------------------------------------
		// Create the content row
		//------------------------------------------------------------
		b.append(sp(in++) + "<tbody>\n");
		//------------------------------------------------------------
		// Display not-installed plug-ins first
		//------------------------------------------------------------
		for(PluginInfo i : l) {
		    if (i.installed == false) {
		        b.append(sp(in) + "<tr class=\"edit\""
		                + " onclick=\"javascript:"
		                + "document.getElementById('"
		                + reqParHashcode + "').value='"
		                + i.getHashcode() + "';"
		                + "document.metrics.submit();\""
		                + ">\n");
		        // Plug-in state
		        b.append(sp(++in) + "<td class=\"trans\">"
		                + "<img src=\"/edit.png\" alt=\"[Edit]\"/>"
		                + "&nbsp;Registered</td>\n");
		        // Plug-in name
		        b.append(sp(in) + "<td class=\"trans\">"
		            + i.getPluginName()
		            + "</td>\n");
		        // Plug-in class
		        b.append(sp(in) + "<td class=\"trans\">"
		                + StringUtils.join((String[]) (
		                        i.getServiceRef().getProperty(
		                                Constants.OBJECTCLASS)),",")
		                                + "</td>\n");
		        // Plug-in version
		        b.append(sp(in) + "<td class=\"trans\">"
		                + i.getPluginVersion() + "</td>\n");
		        b.append(sp(--in) + "</tr>\n");
		        // Extended plug-in information
		        b.append(renderPluginAttributes(
		                i, reqValShowProp, reqValShowActv, in));
		    }
		}
		//------------------------------------------------------------
		// Installed plug-ins
		//------------------------------------------------------------
		for(PluginInfo i : l) {
		    if (i.installed) {
		        b.append(sp(in) + "<tr class=\"edit\""
		                + " onclick=\"javascript:"
		                + "document.getElementById('"
		                + reqParHashcode + "').value='"
		                + i.getHashcode() + "';"
		                + "document.metrics.submit();\""
		                + ">\n");
		        // Plug-in state
		        b.append(sp(++in) + "<td class=\"trans\">"
		                + "<img src=\"/edit.png\" alt=\"[Edit]\"/>"
		                + "&nbsp;Installed</td>\n");
		        // Plug-in name
		        b.append(sp(in) + "<td class=\"trans\">"
		                + i.getPluginName()
		                + "</td>\n");
		        // Plug-in class
		        b.append(sp(in) + "<td class=\"trans\">"
		                + StringUtils.join((String[]) (
		                        i.getServiceRef().getProperty(
		                                Constants.OBJECTCLASS)),",")
		                                + "</td>\n");
		        // Plug-in version
		        b.append(sp(in) + "<td class=\"trans\">"
		                + i.getPluginVersion() + "</td>\n");
		        b.append(sp(--in) + "</tr>\n");
		        // Extended plug-in information
		        b.append(renderPluginAttributes(
		                i, reqValShowProp, reqValShowActv, in));
		    }
		}
		//------------------------------------------------------------
		// Close the table
		//------------------------------------------------------------
		b.append(sp(--in) + "</tbody>\n");
		b.append(sp(--in) + "</table>\n");
		//------------------------------------------------------------
		// Display flags
		//------------------------------------------------------------
		b.append(sp(in) + "<span>\n");
		b.append(sp(++in) + "<input"
		        + " type=\"checkbox\""
		        + ((reqValShowProp) ? "checked" : "")
		        + " onclick=\"javascript:"
		        + "document.getElementById('"
		        + reqParShowProp + "').value = this.checked;"
		        + "document.getElementById('"
		        + reqParHashcode + "').value='';"
		        + "document.metrics.submit();\""
		        + ">Display properties\n");
		b.append(sp(++in) + "<input"
		        + " type=\"checkbox\""
		        + ((reqValShowActv) ? "checked" : "")
		        + " onclick=\"javascript:"
		        + "document.getElementById('"
		        + reqParShowActv + "').value = this.checked;"
		        + "document.getElementById('"
		        + reqParHashcode + "').value='';"
		        + "document.metrics.submit();\""
		        + ">Display activators\n");
		b.append(sp(--in) + "</span>\n");
		// Close the field-set
		b.append(sp(--in) + "</fieldset>\n");
		return in;
	}

	/**
	 * @param b
	 * @param in
	 * @param reqParAction
	 * @param reqParHashcode
	 * @param reqParPropName
	 * @param reqParPropDescr
	 * @param reqParPropType
	 * @param reqParPropValue
	 * @param actValInstall
	 * @param actValUninstall
	 * @param actValSync
	 * @param actValReqAddProp
	 * @param actValReqUpdProp
	 * @param selPI
	 * @return
	 */
	private long pluginEditForm(StringBuilder b, long in, String reqParAction,
			String reqParHashcode, String reqParPropName,
			String reqParPropDescr, String reqParPropType,
			String reqParPropValue, String actValInstall,
			String actValUninstall, String actValSync, String actValReqAddProp,
			String actValReqUpdProp, PluginInfo selPI) {
		// Create the plug-in field-set
		b.append(sp(in) + "<fieldset>\n");
		b.append(sp(++in) + "<legend>"
		        + selPI.getPluginName()
		        + "</legend>\n");
		//------------------------------------------------------------
		// Create the plug-in info table
		//------------------------------------------------------------
		b.append(sp(in) + "<table>\n");
		b.append(sp(++in) + "<thead>\n");
		b.append(sp(++in) + "<tr class=\"head\">\n");
		b.append(sp(++in) + "<td class=\"head\""
		        + " style=\"width: 80px;\">"
		        + "Status</td>\n");
		b.append(sp(in) + "<td class=\"head\""
		        + " style=\"width: 30%;\">"
		        + "Name</td>\n");
		b.append(sp(in) + "<td class=\"head\""
		        + " style=\"width: 40%;\">"
		        + "Class</td>\n");
		b.append(sp(in) + "<td class=\"head\">Version</td>\n");
		b.append(sp(--in) + "</tr>\n");
		b.append(sp(--in) + "</thead>\n");
		// Display the plug-in's info
		b.append(sp(in) + "<tbody>\n");
		b.append(sp(in++) + "<tr>\n");
		// Plug-in state
		b.append(sp(++in) + "<td>"
		        + ((selPI.installed) 
		                ? "Installed" : "Registered")
		                + "</td>\n");
		// Plug-in name
		b.append(sp(in) + "<td>"
		        + selPI.getPluginName() + "</td>\n");
		// Plug-in class
		b.append(sp(in) + "<td>"
		        + StringUtils.join((String[]) (
		                selPI.getServiceRef().getProperty(
		                        Constants.OBJECTCLASS)),",")
		                        + "</td>\n");
		// Plug-in version
		b.append(sp(in) + "<td>"
		        + selPI.getPluginVersion() + "</td>\n");
		b.append(sp(--in) + "</tr>\n");
		// Plug-in tool-bar
		b.append(sp(in++) + "<tr>\n");
		b.append(sp(in++) + "<td colspan=\"4\">\n");
		b.append(sp(in) + "<input type=\"button\""
		        + " class=\"install\""
		        + " style=\"width: 100px;\""
		        + " value=\"Plug-ins list\""
		        + " onclick=\"javascript:"
		        + "document.getElementById('"
		        + reqParHashcode + "').value='';"
		        + "document.metrics.submit();\""
		        + ">\n");
		if (selPI.installed) {
		    b.append(sp(in) + "<input type=\"button\""
		            + " class=\"install\""
		            + " style=\"width: 100px;\""
		            + " value=\"Uninstall\""
		            + " onclick=\"javascript:"
		            + "document.getElementById('"
		            + reqParAction + "').value='"
		            + actValUninstall + "';"
		            + "document.getElementById('"
		            + reqParHashcode +"').value='"
		            + selPI.getHashcode() + "';"
		            + "document.metrics.submit();\""
		            + ">\n");
		    b.append(sp(in) + "<input type=\"button\""
		            + " class=\"install\""
		            + " style=\"width: 100px;\""
		            + " value=\"Synchronise\""
		            + " onclick=\"javascript:"
		            + "document.getElementById('"
		            + reqParAction + "').value='"
		            + actValSync + "';"
		            + "document.getElementById('"
		            + reqParHashcode +"').value='"
		            + selPI.getHashcode() + "';"
		            + "document.metrics.submit();\""
		            + ">\n");
		}
		else {
		    b.append(sp(in) + "<input type=\"button\""
		            + " class=\"install\""
		            + " style=\"width: 100px;\""
		            + " value=\"Install\""
		            + " onclick=\"javascript:"
		            + "document.getElementById('"
		            + reqParAction + "').value='"
		            + actValInstall + "';"
		            + "document.getElementById('"
		            + reqParHashcode +"').value='"
		            + selPI.getHashcode() + "';"
		            + "document.metrics.submit();\""
		            + ">\n");
		}
		b.append(sp(--in) + "</td>\n");
		b.append(sp(--in) + "</tr>\n");
		// Close the plug-in table
		b.append(sp(--in) + "</tbody>\n");
		b.append(sp(--in) + "</table>\n");

		//------------------------------------------------------------
		// Registered metrics, activators and configuration 
		//------------------------------------------------------------
		if (selPI.installed) {
		    //--------------------------------------------------------
		    // Create the metrics field-set
		    //--------------------------------------------------------
		    b.append(sp(++in) + "<fieldset>\n");
		    b.append(sp(++in) + "<legend>"
		            + "Supported metrics"
		            + "</legend>\n");
		    // Create the metrics table
		    b.append(sp(in) + "<table>\n");
		    b.append(sp(++in) + "<thead>\n");
		    b.append(sp(++in) + "<tr class=\"head\">\n");
		    b.append(sp(++in) + "<td class=\"head\""
		            + " style=\"width: 10%;\">"
		            + "Id</td>\n");
		    b.append(sp(in) + "<td class=\"head\""
		            + " style=\"width: 25%;\">"
		            + "Name</td>\n");
		    b.append(sp(in) + "<td class=\"head\""
		            + " style=\"width: 25%;\">"
		            + "Type</td>\n");
		    b.append(sp(in) + "<td class=\"head\""
		            + " style=\"width: 40%;\">"
		            + "Description</td>\n");
		    b.append(sp(--in) + "</tr>\n");
		    b.append(sp(--in) + "</thead>\n");
		    // Display the list of supported metrics
		    b.append(sp(in++) + "<tbody>\n");
		    // Get the list of supported metrics
		    List<Metric> metrics =
		        sobjPA.getPlugin(selPI).getAllSupportedMetrics();
		    if ((metrics == null) || (metrics.isEmpty())) {
		        b.append(sp(in++) + "<tr>");
		        b.append(sp(in) + "<td colspan=\"4\" class=\"noattr\">"
		                + "This plug-in does not support metrics."
		                + "</td>\n");
		        b.append(sp(--in)+ "</tr>\n");
		    }
		    else {
		        for (Metric metric: metrics) {
		            b.append(sp(in++) + "<tr>\n");
		            b.append(sp(in) + "<td>"
		                    + metric.getId() + "</td>\n");
		            b.append(sp(in) + "<td>"
		                    + metric.getMnemonic() + "</td>\n");
		            b.append(sp(in) + "<td>"
		                    + metric.getMetricType().getType()
		                    + "</td>\n");
		            b.append(sp(in) + "<td>"
		                    + metric.getDescription() + "</td>\n");
		            b.append(sp(--in)+ "</tr>\n");
		        }
		    }
		    // Close the metrics table
		    b.append(sp(--in) + "</tbody>\n");
		    // Close the metrics table
		    b.append(sp(--in) + "</table>\n");
		    // Close the metric field-set
		    b.append(sp(--in) + "</fieldset>\n");
		    //--------------------------------------------------------
		    // Create the properties field-set
		    //--------------------------------------------------------
		    b.append(sp(++in) + "<fieldset>\n");
		    b.append(sp(++in) + "<legend>"
		            + "Configuration properties"
		            + "</legend>\n");
		    // Create the properties table
		    b.append(sp(in) + "<table>\n");
		    b.append(sp(++in) + "<thead>\n");
		    b.append(sp(++in) + "<tr class=\"head\">\n");
		    b.append(sp(++in) + "<td class=\"head\""
		            + " style=\"width: 30%;\">"
		            + "Name</td>\n");
		    b.append(sp(in) + "<td class=\"head\""
		            + " style=\"width: 20%;\">"
		            + "Type</td>\n");
		    b.append(sp(in) + "<td class=\"head\""
		            + " style=\"width: 50%;\">"
		            + "Value</td>\n");
		    b.append(sp(--in) + "</tr>\n");
		    b.append(sp(--in) + "</thead>\n");
		    // Display the set of configuration properties
		    b.append(sp(in++) + "<tbody>\n");
		    // Get the plug-in's configuration set
		    Set<PluginConfiguration> config = Plugin.getPluginByHashcode(selPI.getHashcode()).getConfigurations();
		    if ((config == null) || (config.isEmpty())) {
		        b.append(sp(in++) + "<tr>");
		        b.append(sp(in) + "<td colspan=\"3\" class=\"noattr\">"
		                + "This plug-in has no configuration properties."
		                + "</td>\n");
		        b.append(sp(--in)+ "</tr>\n");
		    }
		    else {
		        for (PluginConfiguration param : config) {
		            b.append(sp(in++) + "<tr class=\"edit\""
		                    + " onclick=\"javascript:"
		                    + "document.getElementById('"
		                    + reqParAction + "').value='"
		                    + actValReqUpdProp + "';"
		                    + "document.getElementById('"
		                    + reqParPropName + "').value='"
		                    + param.getName() + "';"
		                    + "document.getElementById('"
		                    + reqParPropType + "').value='"
		                    + param.getType() + "';"
		                    + "document.getElementById('"
		                    + reqParPropDescr + "').value='"
		                    + param.getMsg() + "';"
		                    + "document.getElementById('"
		                    + reqParPropValue + "').value='"
		                    + param.getValue() + "';"
		                    + "document.metrics.submit();\""
		                    + ">\n");
		            // Property's name and description
		            String description = param.getMsg();
		            if (param.getMsg() == null)
		                description = "No description available.";
		            b.append(sp(in) + "<td class=\"trans\""
		                    + " title=\"" + description + "\">"
		                    + "<img src=\"/edit.png\" alt=\"[Edit]\"/>"
		                    + "&nbsp;" + param.getName()
		                    + "</td>\n");
		            // Property's type
		            b.append(sp(in) + "<td class=\"trans\">"
		                    + param.getType()
		                    + "</td>\n");
		            // Property's value
		            b.append(sp(in) + "<td class=\"trans\">"
		                    + param.getValue()
		                    + "</td>\n");
		            b.append(sp(--in)+ "</tr>\n");
		        }
		    }
		    // Command tool-bar
		    b.append(sp(in) + "<tr>\n");
		    b.append(sp(++in) + "<td colspan=\"3\">\n");
		    b.append(sp(++in) + "<input type=\"button\""
		            + " class=\"install\""
		            + " style=\"width: 100px;\""
		            + " value=\"Add property\""
		            + " onclick=\"javascript:"
		            + "document.getElementById('"
		            + reqParAction + "').value='"
		            + actValReqAddProp + "';"
		            + "document.metrics.submit();\""
		            + ">\n");
		    b.append(sp(--in) + "</td>\n");
		    b.append(sp(--in) + "</tr>\n");
		    // Close the properties table
		    b.append(sp(--in) + "</tbody>\n");
		    // Close the properties table
		    b.append(sp(--in) + "</table>\n");
		    // Close the properties field-set
		    b.append(sp(--in) + "</fieldset>\n");
		}

		// Close the plug-in field-set
		b.append(sp(--in) + "</fieldset>\n");
		return in;
	}

	/**
	 * @param b
	 * @param in
	 * @param reqParAction
	 * @param reqParPropName
	 * @param reqParPropDescr
	 * @param reqParPropType
	 * @param reqParPropValue
	 * @param actValConAddProp
	 * @param actValConRemProp
	 * @param reqValPropName
	 * @param reqValPropDescr
	 * @param reqValPropType
	 * @param reqValPropValue
	 * @param selPI
	 * @return
	 */
	private void createUpdateConfigurationProperty(StringBuilder b, long in,
			String reqParAction, String reqParPropName, String reqParPropDescr,
			String reqParPropType, String reqParPropValue,
			String actValConAddProp, String actValConRemProp,
			String reqValPropName, String reqValPropDescr,
			String reqValPropType, String reqValPropValue, PluginInfo selPI) {

		// Check for a property update request
		boolean update = selPI.hasConfProp(
		        reqValPropName, reqValPropType);
		VelocityContext vcLocal = new VelocityContext();
		if(update)
			vcLocal.put("update",update);
		vcLocal.put("updateText", (update) ? "Update property of ": "Create property for ");
		vcLocal.put("pluginName", selPI.getPluginName()+"");
		vcLocal.put("propertyName", reqParPropName);
		vcLocal.put("propertyNameValue", (reqValPropName != null) ? reqValPropName : "");
		vcLocal.put("propertyDescriptionValue",  (reqValPropName != null) ? reqValPropName : "");
		vcLocal.put("propertyDescription", reqParPropDescr);
		vcLocal.put("propertyTypeValue", (reqValPropType != null) ? reqValPropType : "");
		vcLocal.put("propertyType",reqParPropType);
		vcLocal.put("configurationTypes", ConfigurationType.values());
		vcLocal.put("propertyValueValue", (reqValPropValue != null) ? reqValPropValue : "");
		vcLocal.put("propertyValue", reqParPropValue);
		vcLocal.put("createButtonText", ((update) ? "Update" : "Create"));
		vcLocal.put("actionId",reqParAction);
		vcLocal.put("actionRemove",actValConRemProp);
		vcLocal.put("actionAdd", actValConAddProp);
		b.append(velocityContextToString(vcLocal, "createUpdateConfigurationProperty.html"));
	}

	/**
	 * @param e
	 * @param reqValHashcode
	 * @param reqValPropName
	 * @param reqValPropDescr
	 * @param reqValPropType
	 * @param reqValPropValue
	 * @param selPI
	 * @param update
	 * @return
	 */
	private PluginInfo pluginConfigurationUpdateCreateProperty(StringBuilder e,
			String reqValHashcode, String reqValPropName,
			String reqValPropDescr, String reqValPropType,
			String reqValPropValue, PluginInfo selPI, boolean update) {
		if (update) {
		    try {
		        if (selPI.updateConfigEntry(
		                sobjDB,
		                reqValPropName,
		                reqValPropValue)) {
		            // Update the Plug-in Admin's information
		            sobjPA.pluginUpdated(
		                    sobjPA.getPlugin(selPI));
		            // Reload the PluginInfo object
		            selPI =
		                sobjPA.getPluginInfo(reqValHashcode);
		        }
		        else {
		            e.append("Property update"
		                    + " has failed!"
		                    + " Check log for details.");
		        }
		    }
		    catch (Exception ex) {
		        e.append(ex.getMessage());
		    }
		}
		// Create configuration property
		else {
		    try {
		        if (selPI.addConfigEntry(
		                sobjDB,
		                reqValPropName,
		                reqValPropDescr,
		                reqValPropType,
		                reqValPropValue)) {
		            // Update the Plug-in Admin's information
		            sobjPA.pluginUpdated(
		                    sobjPA.getPlugin(selPI));
		            // Reload the PluginInfo object
		            selPI =
		                sobjPA.getPluginInfo(reqValHashcode);
		        }
		        else {
		            e.append("Property creation"
		                    + " has failed!"
		                    + " Check log for details.");
		        }
		    }
		    catch (Exception ex) {
		        e.append(ex.getMessage());
		    }
		}
		return selPI;
	}

	/**
	 * @param e
	 * @param reqValHashcode
	 * @param reqValPropName
	 * @param reqValPropType
	 * @param selPI
	 * @return
	 */
	private PluginInfo pluginConfigPropertyRemoval(StringBuilder e,
			String reqValHashcode, String reqValPropName,
			String reqValPropType, PluginInfo selPI) {
		if (selPI.hasConfProp(
		        reqValPropName, reqValPropType)) {
		    try {
		        if (selPI.removeConfigEntry(
		                sobjDB,
		                reqValPropName,
		                reqValPropType)) {
		            // Update the Plug-in Admin's information
		            sobjPA.pluginUpdated(
		                    sobjPA.getPlugin(selPI));
		            // Reload the PluginInfo object
		            selPI = sobjPA.getPluginInfo(
		                    reqValHashcode);
		        }
		        else {
		            e.append("Property removal"
		                    + " has failed!"
		                    + " Check log for details.");
		        }
		    }
		    catch (Exception ex) {
		        e.append(ex.getMessage());
		    }
		}
		else {
		    e.append ("Unknown configuration property!");
		}
		return selPI;
	}

	/**
	 * @param e
	 * @param reqValHashcode
	 */
	private void pluginUninstallRequest(StringBuilder e, String reqValHashcode) {
		if (sobjPA.uninstallPlugin(reqValHashcode) == false) {
		    e.append("Plug-in can not be uninstalled."
		            + " Check log for details.");
		} else {
		    e.append("A job was scheduled to remove the plug-in");
		}
	}

	/**
	 * @param e
	 * @param reqValHashcode
	 */
	private void pluginInstallRequest(StringBuilder e, String reqValHashcode) {
		if (sobjPA.installPlugin(reqValHashcode) == false) {
		    e.append("Plug-in can not be installed!"
		            + " Check log for details.");
		}
		// Persist the DB changes
		else {
		    PluginInfo pInfo =
		        sobjPA.getPluginInfo(reqValHashcode);
		    sobjPA.pluginUpdated(sobjPA.getPlugin(pInfo));
		}
	}

	/**
	 * @param b
	 * @param in
	 */
	private void noPlugins(StringBuilder b, long in) {
		b.append(normalFieldset(
		        "All plug-ins",
		        null,
		        new StringBuilder("<span>"
		                + "No plug-ins found!&nbsp;"
		                + "<input type=\"button\""
		                + " class=\"install\""
		                + " style=\"width: 100px;\""
		                + " value=\"Refresh\""
		                + " onclick=\"javascript:"
		                + "window.location.reload(true);"
		                + "\">"
		                + "</span>"),
		        in));
	}

    /**
     * Creates a set of table rows populated with the plug-in properties and
     * activators, as found in the given <code>PluginInfo</code> object
     * 
     * @param pluginInfo the plug-in's <code>PluginInfo</code> object
     * @param showProperties display flag
     * @param showActivators display flag
     * @param in indentation value for the generated HTML content
     * 
     * @return The table as HTML presentation.
     */
    private String renderPluginAttributes(
            PluginInfo pluginInfo,
            boolean showProperties,
            boolean showActivators,
            long in) {
        // Stores the assembled HTML content
        StringBuilder b = new StringBuilder();
        // List the metric plug-in's configuration properties
        if (showProperties) {
            Set<PluginConfiguration> l =
                pluginInfo.getConfiguration();
            // Skip if this plug-ins has no configuration
            if ((l != null) && (l.isEmpty() == false)) {
                for (PluginConfiguration property : l) {
                    b.append(sp(in++) + "<tr>");
                    b.append(sp(in) + "<td>&nbsp;</td>\n");
                    b.append(sp(in) + "<td colspan=\"3\" class=\"attr\">"
                            + "<b>Property:</b> " + property.getName()
                            + "&nbsp;<b>Type:</b> " + property.getType()
                            + "&nbsp;<b>Value:</b> " + property.getValue()
                            + "</td>\n");
                    b.append(sp(--in)+ "</tr>\n");
                }
            }
        }
        // List the metric plug-in's activator types
        if (showActivators) {
            Set<Class<? extends DAObject>> activators =
                pluginInfo.getActivationTypes();
            // Skip if this plug-ins has no activators
            if (activators != null) {
                for (Class<? extends DAObject> activator : activators) {
                    b.append("<tr>");
                    b.append("<td>&nbsp;</td>\n");
                    b.append("<td colspan=\"3\" class=\"attr\">"
                            + "<b>Activator:</b> "
                            + activator.getName()
                            + "</td>");
                    b.append("</tr>\n");
                }
            }
        }
        return b.toString();
    }
}

//vi: ai nosi sw=4 ts=4 expandtab
