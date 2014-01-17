/**
 *
 */
package eu.sqooss.test.service.webadmin;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
//import static org.powermock.api.mockito.PowerMockito.do;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.impl.service.webadmin.AdminServlet;
import eu.sqooss.impl.service.webadmin.ProjectsView;
import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.actions.UpdateProject;
import eu.sqooss.service.db.Bug;
import eu.sqooss.service.db.ClusterNode;
import eu.sqooss.service.db.MailMessage;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.scheduler.SchedulerException;

/**
 * @author Ellen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AlitheiaCore.class,StoredProject.class,ClusterNode.class,ProjectVersion.class,MailMessage.class,Bug.class})
public class ProjectsViewTest extends AbstractViewTestBase {

	ProjectsView projectsView;
	BundleContext bundleContext;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		bundleContext = mock(BundleContext.class);
		velocityContext = new VelocityContext();
		projectsView = new ProjectsView(bundleContext, velocityContext);
		super.setUp(projectsView);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link eu.sqooss.impl.service.webadmin.ProjectsView#ProjectsView(org.osgi.framework.BundleContext, org.apache.velocity.VelocityContext)}.
	 * @throws Exception
	 */
	@Test
	public void testAddProject() throws Exception {
		StringBuilder builder = new StringBuilder();
		HttpServletRequest r = mock(HttpServletRequest.class);
		//call private method
		StoredProject proj = Whitebox.<StoredProject>invokeMethod(projectsView, "addProject",r);
		assertThat(proj,equalTo(storedProject));

		//set errors to true
		when(adminAction.hasErrors()).thenReturn(true);
		//call private method addProject with arguments builder,r,0
		proj = Whitebox.<StoredProject>invokeMethod(projectsView, "addProject",r);
		assertThat(proj, nullValue());
	}

	/**
	 * Test method for {@link eu.sqooss.impl.service.webadmin.ProjectsView#ProjectsView(org.osgi.framework.BundleContext, org.apache.velocity.VelocityContext)}.
	 * @throws Exception
	 */
	@Test
	public void testRemoveProject() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		StoredProject proj = Whitebox.<StoredProject>invokeMethod(projectsView, "removeProject",p);
		assertThat(proj,nullValue());

		doThrow(new SchedulerException("Test error")).when(scheduler).enqueue(any(Job.class));;
		proj = Whitebox.<StoredProject>invokeMethod(projectsView, "removeProject",p);

		//call private method addProject with arguments builder,r,0
		proj = Whitebox.<StoredProject>invokeMethod(projectsView, "removeProject",null);
		assertThat(proj, nullValue());
	}

	@Test
	public void testTriggerUpdate() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Whitebox.<StoredProject>invokeMethod(projectsView, "triggerUpdate",p,"mnem");
		verify(adminService).create(UpdateProject.MNEMONIC);

		//set errors to true
		when(adminAction.hasErrors()).thenReturn(true);
		Whitebox.invokeMethod(projectsView, "triggerUpdate",p,"mnem");
		verify(adminService,times(2)).create(UpdateProject.MNEMONIC);
		verify(adminService,times(2)).execute(adminAction);
	}

	@Test
	public void testAllUpdate() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Whitebox.<StoredProject>invokeMethod(projectsView, "triggerAllUpdate",p);

		verify(adminService).create(UpdateProject.MNEMONIC);

		//set errors to true
		when(adminAction.hasErrors()).thenReturn(true);
		Whitebox.invokeMethod(projectsView, "triggerAllUpdate",p);
		verify(adminService,times(2)).create(UpdateProject.MNEMONIC);
		verify(adminService,times(2)).execute(adminAction);
	}

	@Test
	public void testTriggerAllUpdateNode() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Set<StoredProject> set = new HashSet<StoredProject>();
		set.add(p);
		when(clusterNode.getProjects()).thenReturn(set);
		Whitebox.<StoredProject>invokeMethod(projectsView, "triggerAllUpdateNode",p);
		verify(adminService).execute(any(AdminAction.class));

	}

	@Test
	public void testSyncPlugin() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		PluginInfo pluginInfo = mock(PluginInfo.class);
		AlitheiaPlugin alitheiaPlugin = mock(AlitheiaPlugin.class);
		when(pluginAdmin.getPluginInfo("hash")).thenReturn(pluginInfo);
		when(pluginAdmin.getPlugin(pluginInfo)).thenReturn(alitheiaPlugin);
		//call private method
		Whitebox.<StoredProject>invokeMethod(projectsView, "syncPlugin",p,"hash");

		verify(pluginAdmin).getPluginInfo("hash");
		verify(metricActivator).syncMetric(alitheiaPlugin, p);

	}

	@Test
	public void testAddToolBar() throws Exception {
		fail("Rewrite testcase in order to test new method");
		//		StringBuilder builder = new StringBuilder();
//		StoredProject p = mock(StoredProject.class);
//		//call private method
//		Whitebox.<StoredProject>invokeMethod(projectsView, "addToolBar",(StoredProject)null,builder,0l);
//		String expected = "<tr class=\"subhead\">\n  <td>View</td><td colspan=\"6\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n    <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled>\n    <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled>\n  </td>\n<td colspan=\"2\" align=\"right\">\n<input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n";
//		assertThat(builder.toString(), equalTo(expected));
//
//		Whitebox.<StoredProject>invokeMethod(projectsView, "addToolBar",p,builder,0l);
//		expected = "<tr class=\"subhead\">\n  <td>View</td><td colspan=\"6\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n    <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled>\n    <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled>\n  </td>\n<td colspan=\"2\" align=\"right\">\n<input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n<tr class=\"subhead\">\n  <td>View</td><td colspan=\"6\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n    <select name=\"reqUpd\" id=\"reqUpd\" >\n    <optgroup label=\"Import Stage\">    </optgroup>    <optgroup label=\"Parse Stage\">    </optgroup>    <optgroup label=\"Inference Stage\">    </optgroup>    <optgroup label=\"Default Stage\">    </optgroup>    </select>    <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n  </td>\n<td colspan=\"2\" align=\"right\">\n<input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n";
//		assertThat(builder.toString(), equalTo(expected));

	}

	@Test
	public void testCreateFrom() throws Exception {
		VelocityEngine ve = null;
		try {
        	ve = new VelocityEngine();
            ve.setProperty("runtime.log.logsystem.class",
                           "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            ve.setProperty("runtime.log.logsystem.log4j.category",
                           Logger.NAME_SQOOSS_WEBADMIN);
            String resourceLoader = "classpath";
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
            ve.setProperty(resourceLoader + "." + RuntimeConstants.RESOURCE_LOADER + ".class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        }
        catch (Exception e) {
            fail("Failed with exception");
        }
		StringBuilder builder = new StringBuilder();
		StoredProject storedProject = mock(StoredProject.class);
		Whitebox.setInternalState(AdminServlet.class, VelocityEngine.class, ve);
		//call private method
		Whitebox.<StoredProject>invokeMethod(projectsView, "createForm",builder,storedProject,"action");
		String expected = "<h2>$tr.label(\"projects_mngm\")</h2><div id=\"table\"><form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tr>\n    <td colspan=\"6\" class=\"noattr\">\nno_projects</td>\n  </tr>\n  <tr class=\"subhead\">\n    <td>View</td><td colspan=\"6\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n      <select name=\"reqUpd\" id=\"reqUpd\" >\n      <optgroup label=\"Import Stage\">      </optgroup>      <optgroup label=\"Parse Stage\">      </optgroup>      <optgroup label=\"Inference Stage\">      </optgroup>      <optgroup label=\"Default Stage\">      </optgroup>      </select>      <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n    </td>\n  <td colspan=\"2\" align=\"right\">\n  <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n</tbody>\n</table>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n</div>\n<h2>$tr.label(\"install_new_project\")</h2>\n<form id=\"addprojectdir\" method=\"post\" action=\"diraddproject\">\n  <!-- $tr.label(\"project_info_txt\") -->\n  project.properties file location\n  <input name=\"properties\" type=\"text\" alt=\"Enter the path to the project.properties file of the project you want to install\" class=\"form\" size=\"40\"/>\n  <input type=\"submit\" value=\"Install Project\"/>\n</form>\n\n";
		assertEquals(expected.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim(),builder.toString().replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim());
		builder = new StringBuilder();

		String ACT_REQ_SHOW_PROJECT = Whitebox.<String>getInternalState(projectsView,"ACT_REQ_SHOW_PROJECT", ProjectsView.class);
		Whitebox.<StoredProject>invokeMethod(projectsView, "createForm",builder,storedProject,ACT_REQ_SHOW_PROJECT);
		expected = "<h2>$tr.label(\"projects_mngm\")</h2><div id=\"table\"><form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>Project information</legend>\n    <table class=\"borderless\">\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n      <tr>\n        <td colspan=\"2\" class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_back\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>\n  </fieldset>\n <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n</div>\n<h2>$tr.label(\"install_new_project\")</h2>\n<form id=\"addprojectdir\" method=\"post\" action=\"diraddproject\">\n  <!-- $tr.label(\"project_info_txt\") -->\n  project.properties file location\n  <input name=\"properties\" type=\"text\" alt=\"Enter the path to the project.properties file of the project you want to install\" class=\"form\" size=\"40\"/>\n  <input type=\"submit\" value=\"Install Project\"/>\n</form>\n\n";
		assertEquals(expected.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim(),builder.toString().replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim());
		builder = new StringBuilder();

		String ACT_REQ_ADD_PROJECT = Whitebox.<String>getInternalState(projectsView,"ACT_REQ_ADD_PROJECT", ProjectsView.class);
		Whitebox.<StoredProject>invokeMethod(projectsView, "createForm",builder,storedProject,ACT_REQ_ADD_PROJECT);
		expected = "\n<h2>$tr.label(\"projects_mngm\")</h2><div id=\"table\"><form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table class=\"borderless\" width='100%'>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectName\" name=\"projectName\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectHomepage\" name=\"projectHomepage\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectContact\" name=\"projectContact\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectBL\" name=\"projectBL\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectML\" name=\"projectML\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectSCM\" name=\"projectSCM\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n    <tr>\n      <td colspan=\"2\" class=\"borderless\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"project_add\" onclick=\"javascript:document.getElementById('reqAction').value='conAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"cancel\" onclick=\"javascript:document.projects.submit();\">\n      </td>\n    </tr>\n  </table>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n</div>\n<h2>$tr.label(\"install_new_project\")</h2>\n<form id=\"addprojectdir\" method=\"post\" action=\"diraddproject\">\n  <!-- $tr.label(\"project_info_txt\") -->\n  project.properties file location\n  <input name=\"properties\" type=\"text\" alt=\"Enter the path to the project.properties file of the project you want to install\" class=\"form\" size=\"40\"/>\n  <input type=\"submit\" value=\"Install Project\"/>\n</form>\n\n";
		assertEquals(expected.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim(),builder.toString().replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim());
		builder = new StringBuilder();

		String ACT_REQ_REM_PROJECT = Whitebox.<String>getInternalState(projectsView,"ACT_REQ_REM_PROJECT", ProjectsView.class);
		Whitebox.<StoredProject>invokeMethod(projectsView, "createForm",builder,storedProject,ACT_REQ_REM_PROJECT);
		expected = "<h2>$tr.label(\"projects_mngm\")</h2><div id=\"table\"><form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>l0059 : null</legend>\n    <table class=\"borderless\">      <tr>\n        <td class=\"borderless\"><b>delete_project</b></td>\n      </tr>\n      <tr>\n        <td class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0006\" onclick=\"javascript:document.getElementById('reqAction').value='conRemProject';document.projects.submit();\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0004\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>  </fieldset>  <input type='hidden' id='reqAction' name='reqAction' value=''>\n    <input type='hidden' id='projectId' name='projectId' value='0'>\n    <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n  </form>\n</div>\n<h2>$tr.label(\"install_new_project\")</h2>\n<form id=\"addprojectdir\" method=\"post\" action=\"diraddproject\">\n  <!-- $tr.label(\"project_info_txt\") -->\n  project.properties file location\n  <input name=\"properties\" type=\"text\" alt=\"Enter the path to the project.properties file of the project you want to install\" class=\"form\" size=\"40\"/>\n  <input type=\"submit\" value=\"Install Project\"/>\n</form>\n\n";
		assertEquals(expected.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim(),builder.toString().replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim());
		builder = new StringBuilder();

		Set<StoredProject> storedProjects = new HashSet<StoredProject>();
		storedProjects.add(storedProject);
		when(clusterNode.getProjects()).thenReturn(storedProjects);
		ProjectVersion projectVersion = mock(ProjectVersion.class);
		when(ProjectVersion.getLastProjectVersion(storedProject)).thenReturn(projectVersion);
		Whitebox.<StoredProject>invokeMethod(projectsView, "createForm",builder,storedProject,"action");
		expected = "<h2>$tr.label(\"projects_mngm\")</h2><div id=\"table\"><form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tbody>\n    <tr class=\"selected\" onclick=\"javascript:document.getElementById('projectId').value='';document.projects.submit();\">\n      <td class=\"trans\">0</td>\n      <td class=\"trans\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_info\" onclick=\"javascript:document.getElementById('reqAction').value='conShowProject';document.projects.submit();\"> &nbsp;null </td>\n      <td class=\"trans\">0(null)</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">project_not_evaluated</td>\n      <td class=\"trans\">(local)</td>\n    </tr>\n    <tr class=\"subhead\">\n      <td>View</td><td colspan=\"6\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n        <select name=\"reqUpd\" id=\"reqUpd\" >\n        <optgroup label=\"Import Stage\">        </optgroup>        <optgroup label=\"Parse Stage\">        </optgroup>        <optgroup label=\"Inference Stage\">        </optgroup>        <optgroup label=\"Default Stage\">        </optgroup>        </select>        <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n      </td>\n    <td colspan=\"2\" align=\"right\">\n    <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n  </td>\n</tr>\n  </tbody>\n</table>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n</div>\n<h2>$tr.label(\"install_new_project\")</h2>\n<form id=\"addprojectdir\" method=\"post\" action=\"diraddproject\">\n  <!-- $tr.label(\"project_info_txt\") -->\n  project.properties file location\n  <input name=\"properties\" type=\"text\" alt=\"Enter the path to the project.properties file of the project you want to install\" class=\"form\" size=\"40\"/>\n  <input type=\"submit\" value=\"Install Project\"/>\n</form>\n\n";
		assertEquals(expected.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim(),builder.toString().replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim());
		builder = new StringBuilder();

		when(storedProject.isEvaluated()).thenReturn(true);
		when(storedProject.getClusternode()).thenReturn(clusterNode);
		Whitebox.<StoredProject>invokeMethod(projectsView, "createForm",builder,storedProject,"action");
		expected = "<h2>$tr.label(\"projects_mngm\")</h2><div id=\"table\"><form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tbody>\n    <tr class=\"selected\" onclick=\"javascript:document.getElementById('projectId').value='';document.projects.submit();\">\n      <td class=\"trans\">0</td>\n      <td class=\"trans\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_info\" onclick=\"javascript:document.getElementById('reqAction').value='conShowProject';document.projects.submit();\"> &nbsp;null </td>\n      <td class=\"trans\">0(null)</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">project_is_evaluated</td>\n      <td class=\"trans\">null</td>\n    </tr>\n    <tr class=\"subhead\">\n      <td>View</td><td colspan=\"6\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n        <select name=\"reqUpd\" id=\"reqUpd\" >\n        <optgroup label=\"Import Stage\">        </optgroup>        <optgroup label=\"Parse Stage\">        </optgroup>        <optgroup label=\"Inference Stage\">        </optgroup>        <optgroup label=\"Default Stage\">        </optgroup>        </select>        <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n      </td>\n    <td colspan=\"2\" align=\"right\">\n    <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n  </td>\n</tr>\n  </tbody>\n</table>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n</div>\n<h2>$tr.label(\"install_new_project\")</h2>\n<form id=\"addprojectdir\" method=\"post\" action=\"diraddproject\">\n  <!-- $tr.label(\"project_info_txt\") -->\n  project.properties file location\n  <input name=\"properties\" type=\"text\" alt=\"Enter the path to the project.properties file of the project you want to install\" class=\"form\" size=\"40\"/>\n  <input type=\"submit\" value=\"Install Project\"/>\n</form>\n\n";
		assertEquals(expected.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim(),builder.toString().replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim());
		builder = new StringBuilder();

	}

	/**
	 * Test method for {@link eu.sqooss.impl.service.webadmin.ProjectsView#setupVelocityContext(javax.servlet.http.HttpServletRequest)}.
	 */
	@Test
	public void testRender() {
//		VelocityContext vc = new VelocityContext();

		VelocityEngine ve = null;
		try {
        	ve = new VelocityEngine();
            ve.setProperty("runtime.log.logsystem.class",
                           "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            ve.setProperty("runtime.log.logsystem.log4j.category",
                           Logger.NAME_SQOOSS_WEBADMIN);
            String resourceLoader = "classpath";
            ve.setProperty(RuntimeConstants.RESOURCE_LOADER, resourceLoader);
            ve.setProperty(resourceLoader + "." + RuntimeConstants.RESOURCE_LOADER + ".class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        }
        catch (Exception e) {
            fail("Failed with exception");
        }
		Whitebox.setInternalState(AdminServlet.class, VelocityEngine.class, ve);
		HttpServletRequest req = mock(HttpServletRequest.class);
		when(req.getLocale()).thenReturn(Locale.ENGLISH);


		String result = projectsView.setupVelocityContext(req);

		String expected = "\n<h2>$tr.label(\"projects_mngm\")</h2><div id=\"table\"><form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n              <table>\n                <thead>\n                  <tr class=\"head\">\n                    <td class='head'  style='width: 10%;'>Project Id</td>\n                    <td class='head' style='width: 35%;'>Project Name</td>\n                    <td class='head' style='width: 15%;'>Last Version</td>\n                    <td class='head' style='width: 15%;'>Last Email</td>\n                    <td class='head' style='width: 15%;'>Last Bug</td>\n                    <td class='head' style='width: 10%;'>Evaluated</td>\n                    <td class='head' style='width: 10%;'>Host</td>\n                  </tr>\n                </thead>\n              <tr>\n                <td colspan=\"6\" class=\"noattr\">\nNo projects found.</td>\n              </tr>\n              <tr class=\"subhead\">\n                <td>View</td><td colspan=\"6\">\n                  <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n                  <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n                  <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n                  <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled>\n                  <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled>\n                </td>\n              <td colspan=\"2\" align=\"right\">\n              <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n            </td>\n          </tr>\n            </tbody>\n          </table>\n        <input type='hidden' id='reqAction' name='reqAction' value=''>\n        <input type='hidden' id='projectId' name='projectId' value=''>\n        <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n      </form>\n</div>\n<h2>$tr.label(\"install_new_project\")</h2>\n<form id=\"addprojectdir\" method=\"post\" action=\"diraddproject\">\n  <!-- $tr.label(\"project_info_txt\") -->\n  project.properties file location\n  <input name=\"properties\" type=\"text\" alt=\"Enter the path to the project.properties file of the project you want to install\" class=\"form\" size=\"40\"/>\n  <input type=\"submit\" value=\"Install Project\"/>\n</form>\n\n";
		assertEquals(expected.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim(),result.replaceAll("\\t|\\n","").replaceAll(" +"," ").replaceAll("> <","><").trim());
	}

}
