/*
 * Copyright 2008 - Organization for Free and Open Source Software,
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

package eu.sqooss.service.web.services;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import eu.sqooss.impl.service.web.services.MetricManager;
import eu.sqooss.impl.service.web.services.ProjectManager;
import eu.sqooss.impl.service.web.services.UserManager;
import eu.sqooss.impl.service.web.services.datatypes.WSConstants;
import eu.sqooss.impl.service.web.services.datatypes.WSDeveloper;
import eu.sqooss.impl.service.web.services.datatypes.WSDirectory;
import eu.sqooss.impl.service.web.services.datatypes.WSFileGroup;
import eu.sqooss.impl.service.web.services.datatypes.WSFileModification;
import eu.sqooss.impl.service.web.services.datatypes.WSMailMessage;
import eu.sqooss.impl.service.web.services.datatypes.WSMetric;
import eu.sqooss.impl.service.web.services.datatypes.WSMetricType;
import eu.sqooss.impl.service.web.services.datatypes.WSMetricsRequest;
import eu.sqooss.impl.service.web.services.datatypes.WSMetricsResultRequest;
import eu.sqooss.impl.service.web.services.datatypes.WSProjectFile;
import eu.sqooss.impl.service.web.services.datatypes.WSProjectVersion;
import eu.sqooss.impl.service.web.services.datatypes.WSResultEntry;
import eu.sqooss.impl.service.web.services.datatypes.WSShortBug;
import eu.sqooss.impl.service.web.services.datatypes.WSShortMailMessage;
import eu.sqooss.impl.service.web.services.datatypes.WSShortProjectVersion;
import eu.sqooss.impl.service.web.services.datatypes.WSStoredProject;
import eu.sqooss.impl.service.web.services.datatypes.WSTaggedVersion;
import eu.sqooss.impl.service.web.services.datatypes.WSUser;
import eu.sqooss.impl.service.web.services.datatypes.WSUserGroup;
import eu.sqooss.impl.service.web.services.datatypes.WSVersionStats;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.fds.FDSService;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.security.SecurityManager;
import eu.sqooss.service.webadmin.WebadminService;

/*
 * IMPORTANT NOTES:
 *
 * 1. The WebServices's implementation and the data types are specially in this form.
 * The Axis2's wsdl generator is the reason.
 * It doesn't work correct with the interfaces, the abstract classes and the inheritance.
 *
 * 2. java2wsdl doesn't support methods overloading.
 *
 * 3. The returned arrays must not be empty because the client can't parse them.
 * The method can return null then the client receives the array with null element ([null]).
 *
 * The URL is: http:/.../[web.service.context]/services/[web.service.name]
 * The wsdl file is: http:/.../[web.service.context]/services/[web.service.name]?wsdl
 */

/**
 * The <code>WebServices</code> class defines all methods for accessing the
 * SQO-OSS framework information, that are exported in form of web services,
 * for use by the SQO-OSS user interfaces (<i>or other web service aware
 * applications</i>).
 * 
 * @author Evgeni Grigorov, <tt>(ProSyst Software GmbH)</tt>
 * @author Boryan Yotov, <tt>(ProSyst Software GmbH)</tt>
 */
public class WebServices implements EventHandler{

    // Instances of the manager classes
    private MetricManager metricManager;
    private ProjectManager projectManager;
    private UserManager userManager;

    // Instances of the core components
    private Logger logger;
    private PluginAdmin pluginAdmin;
    private SecurityManager securityManager;
    private DBService db;
    private WebadminService wa;
    private FDSService fds;

    /**
     * Instantiates a new WebServices object.
     *
     * @param bc - the parent's bundle context
     * @param securityManager - the Security component's instance
     * @param db - the DB component's instance
     * @param tds - the TDS component's instance
     * @param logger - the Logger component's instance
     * @param wa - the WebAdmin component's instance
     */
    public WebServices(
            BundleContext bc,
            SecurityManager securityManager,
            DBService db,
            PluginAdmin pluginAdmin,
            Logger logger,
            FDSService fds,
            WebadminService wa) {

        this.securityManager = securityManager;
        this.db = db;
        this.pluginAdmin = pluginAdmin;
        this.logger = logger;
        this.wa = wa;
        this.fds = fds;

        //Register an event handler for DB init events
        final String[] topics = new String[] {
                DBService.EVENT_STARTED
        };

        Dictionary<String, String[]> d = new Hashtable<String, String[]>();
        d.put(EventConstants.EVENT_TOPIC, topics );

        bc.registerService(EventHandler.class.getName(), this, d);
    }

    private void initComponents() {
        metricManager = new MetricManager(logger, db, pluginAdmin, securityManager);
        projectManager = new ProjectManager(logger, db, securityManager, fds);
        userManager = new UserManager(logger, securityManager, db, wa);
    }

    // ===[ ProjectManager methods]===========================================

    /**
     * This method returns an array of all projects accessible from the given
     * user, that the SQO-OSS framework has had evaluated.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @return The array of evaluated projects, or a <code>null</code> array
     *   when none are found.
     */
    public WSStoredProject[] getEvaluatedProjects(
            String userName,
            String password) {
        return projectManager.getEvaluatedProjects(userName, password);
    }

    /**
     * This method returns an array of all projects accessible from the given
     * user, no matter if the SQO-OSS framework had evaluated them or not.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     *
     * @return The array of stored projects, or a <code>null</code> array when
     *   none are found.
     */
    public WSStoredProject[] getStoredProjects(
            String userName,
            String password) {
        return projectManager.getStoredProjects(userName, password);
    }

    /**
     * The method returns an array of all file groups that exists in the specified
     * project version.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectVersionId - the project's version identifier
     *
     * @return The array of project's file groups in that project version, or a
     *   <code>null</code> array when none are found.
     */
    public WSFileGroup[] getFileGroupsByProjectVersionId(
            String userName,
            String password,
            long projectVersionId) {
        return projectManager.getFileGroupsByProjectVersionId(
                userName, password, projectVersionId);
    }
    
    /**
     * This method returns the root directory of the specified project's
     * source tree.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectId - the project's identifier
     *
     * @return The root directory's object, or <code>null</code> if not found.
     */
    public WSDirectory getRootDirectory(
            String userName,
            String password,
            long projectId) {
        return projectManager.getRootDirectory(
                userName, password, projectId);
    }

    /**
     * This method returns an array of all files located in the selected
     * directory, that exists in the specified project version.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectVersionId - the project's version identifier
     * @param directoryId - the directory identifier
     *
     * @return The array of project's files in that directory and that project
     * version, or a <code>null</code> array when none are found.
     */
    public WSProjectFile[] getFilesInDirectory(
            String userName,
            String password,
            long projectVersionId,
            long directoryId) {
        return projectManager.getFilesInDirectory(
                userName, password, projectVersionId, directoryId);
    }

    /**
     * The method returns an array of all files that exists in the specified
     * project version. The files' names conform to the regular expression.
     * 
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectVersionId - the project's version identifier
     * @param regExpr - the regular expression
     * 
     * @return The array of project's files in that project version, or a
     *   <code>null</code> array when none are found.
     * The files' names conform to the regular expression.
     */
    public WSProjectFile[] getFilesByRegularExpression(
            String userName,
            String password,
            long projectVersionId,
            String regExpr) {
        return projectManager.getFilesByRegularExpression(userName, password,
                projectVersionId, regExpr);
    }
    
    /**
     * This method returns an array of <code>WSFileModication<code> objects
     * that represent all modification that were performed on the project file
     * with the given Id.
     * 
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectVersionId the project version Id
     * @param projectFileId the project file Id
     * 
     * @return The file's modification history as array,
     *   or a <code>null</code> array when this file can not be found.
     */
    public WSFileModification[] getFileModifications(
            String userName,
            String password,
            long projectVersionId,
            long projectFileId) {
        return projectManager.getFileModifications(
                userName, password, projectVersionId, projectFileId);
    }

    /**
     * The method returns an array representing all evaluated versions of the
     * given project.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectId - the project's identifier
     *
     * @return The array with all evaluated project versions, or a
     *   <code>null</code> array when none are found.
     */
     public WSProjectVersion[] getProjectVersionsByProjectId(
            String userName,
            String password,
            long projectId) {
        return projectManager.getProjectVersionsByProjectId(
                userName, password, projectId);
    }

    /**
     * The method returns an array that contains all tagged versions in the
     * specified project.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectId - the project's identifier
     *
     * @return The array with all tagged project versions, or a
     *   <code>null</code> array when none are found.
     */
    public WSTaggedVersion[] getTaggedVersionsByProjectId(
            String userName,
            String password,
            long projectId) {
        return projectManager.getTaggedVersionsByProjectId(
                userName, password, projectId);
    }

    /**
     * The method returns all information, that the SQO-OSS framework has
     * collected about the specified project versions.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectVerionsIds - the project versions' identifiers
     *
     * @return The <code>WSProjectVersion</code> array that describes the
     * project versions, or <code>null</code> when such project versions do not exist.
     */
    public WSProjectVersion[] getProjectVersionsByIds(
            String userName,
            String password,
            long[] projectVersionsIds) {
        return projectManager.getProjectVersionsByIds(
                userName, password, projectVersionsIds);
    }

    /**
     * This method returns file statistic per given project version.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectVerionsIds - the list of project versions' identifiers
     *
     * @return The <code>WSVersionStats</code> array of objects which describe
     *   the file statistics for the given project versions,
     *   or <code>null</code> when none of the given project versions exist.
     */
    public WSVersionStats[] getVersionsStatistics(
            String userName,
            String password,
            long[] projectVersionsIds) {
        return projectManager.getVersionsStatistics(
                userName, password, projectVersionsIds);
    }

    /**
     * This method returns the list of project versions which carry the given
     * time stamps.
     *
     * @param userName - the user's name (<i>used for authentication</i>)
     * @param password - the user's password (<i>used for authentication</i>)
     * @param projectId - the project identifier
     * @param timestamps - the list of time stamps
     *
     * @return The array of <code>WSProjectVersion</code> objects for all
     *  matching project versions, or <code>null</code> if none were found.
     */
    public WSProjectVersion[] getProjectVersionsByTimestamps(
            String userName,
            String password,
            long projectId,
            long[] timestamps) {
        return projectManager.getProjectVersionsByTimestamps(
                userName, password, projectId, timestamps);
    }

    /**
     * This method returns the list of project versions which carry the given
     * SCM version Ids.
     *
     * @param userName - the user's name (<i>used for authentication</i>)
     * @param password - the user's password (<i>used for authentication</i>)
     * @param projectId - the project identifier
     * @param scmIds - the list of SCM version Ids
     *
     * @return The array of <code>WSProjectVersion</code> objects for all
     *  matching project versions, or <code>null</code> if none were found.
     */
    public WSProjectVersion[] getProjectVersionsByScmIds(
            String userName,
            String password,
            long projectId,
            String[] scmIds) {
        return projectManager.getProjectVersionsByScmIds(
                userName, password, projectId, scmIds);
    }

    /**
     * The method returns all information, that the SQO-OSS framework has
     * collected about the first versions (probably SVN revision 1) 
     * of each of the projects.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectsIds - the projects' identifiers
     *
     * @return The <code>WSProjectVersion</code> array that describes the
     * project versions, or <code>null</code> when such project versions do not exist.
     */
    public WSProjectVersion[] getFirstProjectVersions(String userName,
            String password, long[] projectsIds) {
        return projectManager.getFirstProjectVersions(
                userName, password, projectsIds);
    }

    /**
     * The method returns all information, that the SQO-OSS framework has
     * collected about the last versions of the projects.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectsIds - the projects' identifiers
     *
     * @return The <code>WSProjectVersion</code> array that describes the
     * project versions, or <code>null</code> when such project versions do not exist.
     */
    public WSProjectVersion[] getLastProjectVersions(String userName,
            String password, long[] projectsIds) {
        return projectManager.getLastProjectVersions(
                userName, password, projectsIds);
    }

    // TODO: JavaDoc
    public WSProjectVersion getPreviousVersionById(
            String userName,
            String password,
            long versionId) {
        return projectManager.getPreviousVersionById(
                userName, password, versionId);
    }

    // TODO: JavaDoc
    public WSProjectVersion getNextVersionById(
            String userName,
            String password,
            long versionId) {
        return projectManager.getNextVersionById(
                userName, password, versionId);
    }

    /**
     * Returns the total number of versions for the given project.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectId - the project's identifier
     *
     * @return The total number of version for that project.
     */
    public long getVersionsCount(
            String userName, String password, long projectId) {
        return projectManager.getVersionsCount(
                userName, password, projectId);
    }

    /**
     * The method returns all information, that the SQO-OSS framework has
     * collected about the specified projects.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectsIds - the projects' identifiers
     *
     * @return The <code>WSStoredProject</code> array that describes the
     * projects, or <code>null</code> when such projects do not exist.
     */
    public WSStoredProject[] getProjectsByIds(
            String userName,
            String password,
            long[] projectsIds) {
        return projectManager.getProjectsByIds(userName, password, projectsIds);
    }

    /**
     * The method returns all information, that the SQO-OSS framework has
     * collected about the specified project.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectName - the project's name
     *
     * @return The <code>WSStoredProject</code> object that describes the
     * project, or <code>null</code> when such project does not exist.
     */
    public WSStoredProject getProjectByName(
            String userName,
            String password,
            String projectName) {
        return projectManager.getProjectByName(
                userName, password, projectName);
    }

    /**
     * The method returns the total number of files, that exists in the given
     * project version.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectVersionId - the project's version identifier
     *
     * @return The number of project's files in that project version.
     */
    public long getFilesNumberByProjectVersionId(
            String userName,
            String password,
            long projectVersionId) {
        return projectManager.getFilesNumberByProjectVersionId(
                userName, password, projectVersionId);
    }

    /**
     * This method returns all known information about the directories referenced by
     * the given identifiers.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param directoriesIds - the identifiers of the requested directories
     *
     * @return The <code>WSDirectory</code> array describing the requested directories.
     */
    public WSDirectory[] getDirectoriesByIds(
            String userName,
            String password,
            long[] directoriesIds) {
        return projectManager.getDirectoriesByIds(
                userName, password, directoriesIds);
    }

    /**
     * This method returns all known information about the developers referenced by
     * the given identifiers.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param developersIds - the identifiers of the requested developers
     *
     * @return The <code>WSDeveloper</code> array describing the requested developers.
     */
    public WSDeveloper[] getDevelopersByIds(
            String userName,
            String password,
            long[] developersIds) {
        return projectManager.getDevelopersByIds(userName,
                password, developersIds);
    }

    // ===[ MetricManager methods]============================================

    /**
     * This method returns an array with all metrics, that have been evaluated
     * for the given project.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param projectId - the project's identifier
     *
     * @return The array with all evaluated metrics, or a <code>null</code>
     *   array when none are found.
     */
    public WSMetric[] getProjectEvaluatedMetrics(
            String userName,
            String password,
            long projectId) {
        return metricManager.getProjectEvaluatedMetrics(
                userName, password, projectId);
    }

    /**
     * The method returns all information, that the SQO-OSS framework has
     * collected about the specified metric types.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param metricTypesIds - the metric types' identifiers
     *
     * @return The <code>WSMetricType</code> array that describes the
     * metric types, or <code>null</code> when such metric types do not exist.
     */
    public WSMetricType[] getMetricTypesByIds(
            String userName,
            String password,
            long[] metricTypesIds) {
        return metricManager.getMetricTypesByIds(
                userName, password, metricTypesIds);
    }

    /**
     * This method performs a metrics lookup following the criteria specified
     * in the given request object.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param request - the request object
     *
     * @return The array of metrics,
     * or a <code>null</code> array when none are found.
     */
    public WSMetric[] getMetricsByResourcesIds(
            String userName,
            String password,
            WSMetricsRequest request) {
        return metricManager.getMetricsByResourcesIds(userName, password, request);
    }

    /**
     * This method will return the list of all metrics that are currently
     * registered in the SQO-OSS framework.
     * 
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * 
     * @return The array with all metrics, or a <code>null<code> when none
     *   are found.
     */
    public WSMetric[] getAllMetrics(
            String userName,
            String password) {
        return metricManager.getAllMetrics(userName, password);
    }

    /**
     * Returns the array of results from the evaluation of the specified
     * metrics on the given data access object.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param resultRequest - the request object,
     * the object contains the request information
     *
     * @return The array of all metric evaluation results on that request,
     * or a <code>null</code> array when none are found.
     */
    public WSResultEntry[] getMetricsResult(String userName, String password,
            WSMetricsResultRequest resultRequest) {
        return metricManager.getMetricsResult(userName, password, resultRequest);
    }

    // ===[ UserManager methods]==============================================

    /**
     * This method creates a new pending user entry, and sends an email to the
     * given user address with a request for confirmation. After successful
     * confirmation, the pending user entry is converted into a SQO-OSS user.
     * <br/>
     * Note: If the user doesn't confirm the request in time, then the pending
     * user entry is automatically removed from the system, after its
     * expiration.
     *
     * @param userNameForAccess - the SQO-OSS unprivileged user's name
     * @param passwordForAccess - the SQO-OSS unprivileged user's password
     * @param newUserName - name for the new user
     * @param newPassword - password of the new user
     * @param email - email address of the new user
     *
     * @return <code>true</code> upon success, or <code>false</code> when a
     *   user with the same name already exists.
     */
    public boolean createPendingUser(
            String userNameForAccess,
            String passwordForAccess,
            String newUserName,
            String newPassword,
            String email) {
        return userManager.createPendingUser(userNameForAccess, passwordForAccess,
                newUserName, newPassword, email);
    }

    /**
     * This method returns all known information about the users referenced by
     * the given identifiers.
     * <br/>
     * <i>The information does not include the users' password hash.<i>
     *
     * @param userNameForAccess - the user's name used for authentication
     * @param passwordForAccess - the user's password used for authentication
     * @param usersIds - the identifiers of the requested users
     *
     * @return The <code>WSUser</code> array describing the requested users.
     */
    public WSUser[] getUsersByIds(
            String userNameForAccess,
            String passwordForAccess,
            long[] usersIds) {
        return userManager.getUsersByIds(
                userNameForAccess, passwordForAccess, usersIds);
    }

    /**
     * The method returns all information, that the SQO-OSS framework has
     * collected about the users' groups
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     *
     * @return The <code>WSUserGroup</code> array that describes the
     * users' groups, or <code>null</code> when the groups do not exist.
     */
    public WSUserGroup[] getUserGroups(
            String userName,
            String password) {
        return userManager.getUserGroups(userName, password);
    }

    /**
     * This method returns all known information about the user associated
     * with the given user name.
     * <br/>
     * <i>The information does not include the user's password hash.<i>
     *
     * @param userNameForAccess - the user's name used for authentication
     * @param passwordForAccess - the user's password used for authentication
     * @param userName - the name of the requested user
     *
     * @return The <code>WSUser</code> object describing the requested user.
     */
    public WSUser getUserByName(
            String userNameForAccess,
            String passwordForAccess,
            String userName) {
        return userManager.getUserByName(
                userNameForAccess, passwordForAccess, userName);
    }

    /**
     * This method modifies the information of the existing user associated
     * with the given user name.
     * <br/>
     * <i>This method can change the user's password and email address
     *   only.</i>
     *
     * @param userNameForAccess - the user's name used for authentication
     * @param passwordForAccess - the user's password used for authentication
     * @param userName - the name of the requested user
     * @param newPassword - the new password
     * @param newEmail - the new email address
     *
     * @return <code>true</code> upon successful modification,
     *   or <code>false</code> in case of failure.
     */
    public boolean modifyUser(
            String userNameForAccess,
            String passwordForAccess,
            String userName,
            String newPassword,
            String newEmail) {
        return userManager.modifyUser(
                userNameForAccess,
                passwordForAccess,
                userName,
                newPassword,
                newEmail);
    }

    /**
     * This method deletes the user referenced by the given identifier
     *
     * @param userNameForAccess - the user's name used for authentication
     * @param passwordForAccess - the user's password used for authentication
     * @param userId - the identifier of the requested user
     *
     * @return <code>true</code> upon successful removal,
     *   or <code>false</code> in case of failure.
     */
    public boolean deleteUserById(
            String userNameForAccess,
            String passwordForAccess,
            long userId) {
        return userManager.deleteUserById(
                userNameForAccess, passwordForAccess, userId);
    }

    //========================================================================
    // TIMELINE RELATED PROJECT METHODS
    //========================================================================

    /**
     * This method will return the list of project versions, associated to
     * project related events which had happened during the given time period
     * (<i>specified using the <code>tsmFrom<code> and <code>tsmTill</code>
     * timestamps</i>).
     * 
     * @param userName the user's name used for authentication
     * @param password the user's password used for authentication
     * @param projectId the project's identifier
     * @param tsmFrom the timestamp of the period begin
     * @param tsmTill the timestamp of the period end
     * 
     * @return The array of <code>WSProjectVersion</code> objects that
     * describe all located project versions, or <code>null</code> when no
     * version related events exist in the given time period.
     */
    public WSProjectVersion[] getSCMTimeline(String userName,
            String password, long projectId, long tsmFrom, long tsmTill) {
        return projectManager.getSCMTimeline(
                userName, password, projectId, tsmFrom, tsmTill);
    }

    /**
     * This method will return the list of project versions, associated to
     * project related events which had happened during the given time period
     * (<i>specified using the <code>tsmFrom<code> and <code>tsmTill</code>
     * timestamps</i>).
     * <br/>
     * <br/>
     * <i><b>NOTE:</b> This method uses the short version wrapper class.</i>
     * 
     * @param userName the user's name used for authentication
     * @param password the user's password used for authentication
     * @param projectId the project's identifier
     * @param tsmFrom the timestamp of the period begin
     * @param tsmTill the timestamp of the period end
     * 
     * @return The array of <code>WSProjectVersion</code> objects that
     * describe all located project versions, or <code>null</code> when no
     * version related events exist in the given time period.
     */
    public WSShortProjectVersion[] getShortSCMTimeline(String userName,
            String password, long projectId, long tsmFrom, long tsmTill) {
        return projectManager.getShortSCMTimeline(
                userName, password, projectId, tsmFrom, tsmTill);
    }

    /**
     * This method will return the list of email messages, associated to
     * project related events which had happened during the given time period
     * (<i>specified using the <code>tsmFrom<code> and <code>tsmTill</code>
     * timestamps</i>).
     * 
     * @param userName the user's name used for authentication
     * @param password the user's password used for authentication
     * @param projectId the project's identifier
     * @param tsmFrom the timestamp of the period begin
     * @param tsmTill the timestamp of the period end
     * 
     * @return The array of <code>WSMailMessage</code> objects that describe
     * all located email messages, or <code>null</code> when no email related
     * events exist in the given time period.
     */
    public WSMailMessage[] getMailTimeline(String userName,
            String password, long projectId, long tsmFrom, long tsmTill) {
        return projectManager.getMailTimeline(
                userName, password, projectId, tsmFrom, tsmTill);
    }

    /**
     * This method will return the list of email messages, associated to
     * project related events which had happened during the given time period
     * (<i>specified using the <code>tsmFrom<code> and <code>tsmTill</code>
     * timestamps</i>).
     * <br/>
     * <br/>
     * <i><b>NOTE:</b> This method uses the short email wrapper class.</i>
     * 
     * @param userName the user's name used for authentication
     * @param password the user's password used for authentication
     * @param projectId the project's identifier
     * @param tsmFrom the timestamp of the period begin
     * @param tsmTill the timestamp of the period end
     * 
     * @return The array of <code>WSMailMessage</code> objects that describe
     * all located email messages, or <code>null</code> when no email related
     * events exist in the given time period.
     */
    public WSShortMailMessage[] getShortMailTimeline(String userName,
            String password, long projectId, long tsmFrom, long tsmTill) {
        return projectManager.getShortMailTimeline(
                userName, password, projectId, tsmFrom, tsmTill);
    }

    /**
     * This method will return the list of bug entries, associated to
     * project related events which had happened during the given time period
     * (<i>specified using the <code>tsmFrom<code> and <code>tsmTill</code>
     * timestamps</i>).
     * <br/>
     * <br/>
     * <i><b>NOTE:</b> This method uses the short bug wrapper class.</i>
     * 
     * @param userName the user's name used for authentication
     * @param password the user's password used for authentication
     * @param projectId the project's identifier
     * @param tsmFrom the timestamp of the period begin
     * @param tsmTill the timestamp of the period end
     * 
     * @return The array of <code>WSBug</code> objects that describe
     * all located bug entries, or <code>null</code> when no bug related
     * events exist in the given time period.
     */
    public WSShortBug[] getShortBugTimeline(String userName,
            String password, long projectId, long tsmFrom, long tsmTill) {
        return projectManager.getShortBugTimeline(
                userName, password, projectId, tsmFrom, tsmTill);
    }
    //========================================================================
    // MISCELANEOUS METHODS
    //========================================================================

    /**
     * Returns the user's message of the day. MOTD's are usually created by
     * the SQO-OSS system administrator or the SQO-OSS framework itself,
     * upon occurrence of specific events (like addition of a new project).
     *
     * @param userName - the user's name
     * @param password - the user's password
     *
     * @return The message of the day, which is valid for that user.
     */
    public String getMessageOfTheDay(
            String userName,
            String password) {
        return userManager.getMessageOfTheDay(userName, password);
    }

    /**
     * The method notifies the administrator of the framework.
     * The user receives the status of the message.
     *
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * @param title    - the title of the message
     * @param message  - the notification message
     *
     * @return <code>true</code> if the message is queued
     */
    public boolean notifyAdmin(
            String userName,
            String password,
            String title,
            String messageBody) {
        return userManager.notifyAdmin(userName, password, title, messageBody);
    }

    /**
     * The method returns all shared constants from the Alitheia system.
     * 
     * @param userName - the user's name used for authentication
     * @param password - the user's password used for authentication
     * 
     * @return the shared constants
     */
    public WSConstants getConstants(String userName, String password) {
        return userManager.getConstants(userName, password);
    }
    
    // ===[EventHandler method]===============================================
    public void handleEvent(Event e) {
        logger.debug("Caught EVENT type=" + e.getPropertyNames().toString());
        if (e.getTopic() == DBService.EVENT_STARTED) {
            initComponents();
        }
    }
}

// vi: ai nosi sw=4 ts=4 expandtab
