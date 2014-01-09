/**
 * 
 */
package eu.sqooss.test.service.webadmin;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
//import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
//import static org.powermock.api.mockito.PowerMockito.do;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.impl.service.webadmin.AbstractView;
import eu.sqooss.impl.service.webadmin.ProjectsView;
import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.admin.AdminAction;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.admin.actions.AddProject;
import eu.sqooss.service.admin.actions.UpdateProject;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.Bug;
import eu.sqooss.service.db.ClusterNode;
import eu.sqooss.service.db.MailMessage;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.scheduler.SchedulerException;
import eu.sqooss.service.updater.UpdaterService;

/**
 * @author Ellen
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AlitheiaCore.class,StoredProject.class,ClusterNode.class,ProjectVersion.class,MailMessage.class,Bug.class})
public class ProjectsViewTest {

	private AlitheiaCore alitheiaCore;
	private AdminService adminService;
	private AdminAction adminAction;
	private PluginAdmin pluginAdmin;
	private VelocityContext veclocityContext;
	private StoredProject storedProject;
	private Scheduler scheduler;
	private ClusterNode clusterNode;
	private MetricActivator metricActivator;
	private Logger logger;
	private ClusterNodeService clusterNodeService;
	private UpdaterService updateService;
	private MailMessage mailMessage;
	private Bug bug;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		//create mocks
		mockStatic(AlitheiaCore.class);
		mockStatic(StoredProject.class);
		mockStatic(ClusterNode.class);
		mockStatic(ProjectVersion.class);
		mockStatic(MailMessage.class);
		mockStatic(Bug.class);
		
		alitheiaCore = mock(AlitheiaCore.class);
		adminService = mock(AdminService.class);
		adminAction = mock(AdminAction.class);
		veclocityContext = mock(VelocityContext.class);
		storedProject = mock(StoredProject.class);
		scheduler = mock(Scheduler.class);
		clusterNode = mock(ClusterNode.class);
		pluginAdmin = mock(PluginAdmin.class);
		metricActivator = mock(MetricActivator.class);
		logger = mock(Logger.class);
		clusterNodeService = mock(ClusterNodeService.class);
		updateService = mock(UpdaterService.class);
		mailMessage = mock(MailMessage.class);
		bug = mock(Bug.class);
		
		//set private static fields
		Whitebox.setInternalState(AbstractView.class, VelocityContext.class, veclocityContext);
		Whitebox.setInternalState(AbstractView.class, Scheduler.class, scheduler);
		Whitebox.setInternalState(AbstractView.class, PluginAdmin.class, pluginAdmin);
		Whitebox.setInternalState(AbstractView.class, MetricActivator.class, metricActivator);
		Whitebox.setInternalState(AbstractView.class, Logger.class, logger);
		Whitebox.setInternalState(AbstractView.class, ClusterNodeService.class, clusterNodeService);
		Whitebox.setInternalState(AbstractView.class, UpdaterService.class, updateService);

		//define behavior public static method calls
		when(AlitheiaCore.getInstance()).thenReturn(alitheiaCore);
		when(StoredProject.getProjectByName(anyString())).thenReturn(storedProject);
		when(ClusterNode.thisNode()).thenReturn(clusterNode);
		when(MailMessage.getLatestMailMessage(any(StoredProject.class))).thenReturn(mailMessage);
		when(Bug.getLastUpdate(any(StoredProject.class))).thenReturn(bug);

		
		//define behavior public method calls
		when(alitheiaCore.getAdminService()).thenReturn(adminService);
		when(alitheiaCore.getPluginAdmin()).thenReturn(pluginAdmin);
		when(adminService.create(AddProject.MNEMONIC)).thenReturn(adminAction);
		when(adminService.create(UpdateProject.MNEMONIC)).thenReturn(adminAction);
		Map<String,Object> map = new HashMap<String,Object>();
		when(adminAction.results()).thenReturn(map);
		when(adminAction.errors()).thenReturn(null);
		when(clusterNodeService.getClusterNodeName()).thenReturn("ClusterNodeName");
		
		//call constructor
//		new ProjectsView(mock(BundleContext.class), mock(VelocityContext.class));
		
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
		StoredProject proj = Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "addProject",builder,r,0);
		assertThat(proj,equalTo(storedProject));
		
		//set errors to true
		when(adminAction.hasErrors()).thenReturn(true);
		//call private method addProject with arguments builder,r,0
		proj = Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "addProject",builder,r,0);
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
		StoredProject proj = Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "removeProject",builder,p,0);
		assertThat(proj,nullValue());
		
		doThrow(new SchedulerException("Test error")).when(scheduler).enqueue(any(Job.class));;
		proj = Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "removeProject",builder,p,0);

		//call private method addProject with arguments builder,r,0
		proj = Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "removeProject",builder,null,0);
		assertThat(proj, nullValue());
	}
	
	@Test
	public void testTriggerUpdate() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "triggerUpdate",builder,p,0,"mnem");
		verify(adminService).create(UpdateProject.MNEMONIC);
		verify(veclocityContext).put("RESULTS", adminAction.results());
		
		//set errors to true
		when(adminAction.hasErrors()).thenReturn(true);
		Whitebox.invokeMethod(ProjectsView.class, "triggerUpdate",builder,p,0,"mnem");
		verify(adminService,times(2)).create(UpdateProject.MNEMONIC);
		verify(adminService,times(2)).execute(adminAction);
		verify(veclocityContext).put("RESULTS", adminAction.errors());
	}
	
	@Test
	public void testAllUpdate() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "triggerAllUpdate",builder,p,0);
		
		verify(adminService).create(UpdateProject.MNEMONIC);
		verify(veclocityContext).put("RESULTS", adminAction.results());
		
		//set errors to true
		when(adminAction.hasErrors()).thenReturn(true);
		Whitebox.invokeMethod(ProjectsView.class, "triggerAllUpdate",builder,p,0);
		verify(adminService,times(2)).create(UpdateProject.MNEMONIC);
		verify(adminService,times(2)).execute(adminAction);
		verify(veclocityContext).put("RESULTS", adminAction.errors());
	}
	
	@Test
	public void testTriggerAllUpdateNode() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Set<StoredProject> set = new HashSet<StoredProject>();
		set.add(p);
		when(clusterNode.getProjects()).thenReturn(set);
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "triggerAllUpdateNode",builder,p,0);
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
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "syncPlugin",builder,p,"hash");
		
		verify(pluginAdmin).getPluginInfo("hash");
		verify(metricActivator).syncMetric(alitheiaPlugin, p);
		
	}
	
	@Test
	public void testAddHiddenFields() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "addHiddenFields",p,builder,0l);
		String expected = "<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n";
		assertThat(builder.toString(), equalTo(expected));
		
	}
	
	@Test
	public void testAddToolBar() throws Exception {
		StringBuilder builder = new StringBuilder();
		StoredProject p = mock(StoredProject.class);
		//call private method
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "addToolBar",(StoredProject)null,builder,0l);
		String expected = "<tr class=\"subhead\">\n  <td>View</td><td colspan=\"6\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n    <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled>\n    <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled>\n  </td>\n<td colspan=\"2\" align=\"right\">\n<input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n";
		assertThat(builder.toString(), equalTo(expected));
		
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "addToolBar",p,builder,0l);
		expected = "<tr class=\"subhead\">\n  <td>View</td><td colspan=\"6\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n    <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled>\n    <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled>\n  </td>\n<td colspan=\"2\" align=\"right\">\n<input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n<tr class=\"subhead\">\n  <td>View</td><td colspan=\"6\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n    <select name=\"reqUpd\" id=\"reqUpd\" >\n    <optgroup label=\"Import Stage\">    </optgroup>    <optgroup label=\"Parse Stage\">    </optgroup>    <optgroup label=\"Inference Stage\">    </optgroup>    <optgroup label=\"Default Stage\">    </optgroup>    </select>    <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n    <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n  </td>\n<td colspan=\"2\" align=\"right\">\n<input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n";
		assertThat(builder.toString(), equalTo(expected));
		
	}
	
	@Test
	public void testShowLastAppliedVersion() throws Exception {
		StringBuilder builder = new StringBuilder();
		Collection<PluginInfo> col = new HashSet<PluginInfo>();
		StoredProject p = mock(StoredProject.class);
		PluginInfo pluginInfo = mock(PluginInfo.class);
		pluginInfo.installed = true;
		col.add(pluginInfo);

		//call private method
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "showLastAppliedVersion",p,col,builder);
		String expected = "<tr>\n  <td colspan=\"7\" class=\"noattr\">\n<input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Synchronise\" onclick=\"javascript:document.getElementById('reqParSyncPlugin').value='null';document.projects.submit();\">&nbsp;null</td>\n</tr>\n";
		
		verify(pluginInfo).getHashcode();
		verify(pluginInfo).getPluginName();
		assertThat(builder.toString(), equalTo(expected));
	}
	
	@Test
	public void testAddHeaderRow() throws Exception {
		StringBuilder builder = new StringBuilder();
		//call private method
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "addHeaderRow",builder,0l);
		String expected = "<table>\n  <thead>\n    <tr class=\"head\">\n      <td class='head'  style='width: 10%;'>l0066</td>\n      <td class='head' style='width: 35%;'>l0067</td>\n      <td class='head' style='width: 15%;'>l0068</td>\n      <td class='head' style='width: 15%;'>l0069</td>\n      <td class='head' style='width: 15%;'>l0070</td>\n      <td class='head' style='width: 10%;'>l0071</td>\n      <td class='head' style='width: 10%;'>l0073</td>\n    </tr>\n  </thead>\n";
		assertThat(builder.toString(), equalTo(expected));
	}
	
	@Test
	public void testCreateFrom() throws Exception {
		StringBuilder builder = new StringBuilder();
		StringBuilder builder2 = new StringBuilder();
		StoredProject storedProject = mock(StoredProject.class);
		//call private method
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "createFrom",builder,builder2,storedProject,"action",0);
		String expected = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tr>\n    <td colspan=\"6\" class=\"noattr\">\nno_projects</td>\n  </tr>\n  <tr class=\"subhead\">\n    <td>View</td><td colspan=\"6\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n      <select name=\"reqUpd\" id=\"reqUpd\" >\n      <optgroup label=\"Import Stage\">      </optgroup>      <optgroup label=\"Parse Stage\">      </optgroup>      <optgroup label=\"Inference Stage\">      </optgroup>      <optgroup label=\"Default Stage\">      </optgroup>      </select>      <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n    </td>\n  <td colspan=\"2\" align=\"right\">\n  <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n</tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n";
		String expected2 = "";
		assertThat(builder.toString(), equalTo(expected));
		assertThat(builder2.toString(),equalTo(expected2));
		
		String ACT_REQ_SHOW_PROJECT = Whitebox.<String>getInternalState(ProjectsView.class,"ACT_REQ_SHOW_PROJECT", ProjectsView.class);
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "createFrom",builder,builder2,storedProject,ACT_REQ_SHOW_PROJECT,0);
		expected = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tr>\n    <td colspan=\"6\" class=\"noattr\">\nno_projects</td>\n  </tr>\n  <tr class=\"subhead\">\n    <td>View</td><td colspan=\"6\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n      <select name=\"reqUpd\" id=\"reqUpd\" >\n      <optgroup label=\"Import Stage\">      </optgroup>      <optgroup label=\"Parse Stage\">      </optgroup>      <optgroup label=\"Inference Stage\">      </optgroup>      <optgroup label=\"Default Stage\">      </optgroup>      </select>      <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n    </td>\n  <td colspan=\"2\" align=\"right\">\n  <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n</tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>Project information</legend>\n    <table class=\"borderless\">\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n      <tr>\n        <td colspan=\"2\" class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_back\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>\n  </fieldset>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n";
		expected2 = "";
		assertThat(builder.toString(), equalTo(expected));
		assertThat(builder2.toString(),equalTo(expected2));
		
		String ACT_REQ_ADD_PROJECT = Whitebox.<String>getInternalState(ProjectsView.class,"ACT_REQ_ADD_PROJECT", ProjectsView.class);
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "createFrom",builder,builder2,storedProject,ACT_REQ_ADD_PROJECT,0);
		expected = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tr>\n    <td colspan=\"6\" class=\"noattr\">\nno_projects</td>\n  </tr>\n  <tr class=\"subhead\">\n    <td>View</td><td colspan=\"6\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n      <select name=\"reqUpd\" id=\"reqUpd\" >\n      <optgroup label=\"Import Stage\">      </optgroup>      <optgroup label=\"Parse Stage\">      </optgroup>      <optgroup label=\"Inference Stage\">      </optgroup>      <optgroup label=\"Default Stage\">      </optgroup>      </select>      <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n    </td>\n  <td colspan=\"2\" align=\"right\">\n  <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n</tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>Project information</legend>\n    <table class=\"borderless\">\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n      <tr>\n        <td colspan=\"2\" class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_back\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>\n  </fieldset>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table class=\"borderless\" width='100%'>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectName\" name=\"projectName\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectHomepage\" name=\"projectHomepage\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectContact\" name=\"projectContact\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectBL\" name=\"projectBL\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectML\" name=\"projectML\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectSCM\" name=\"projectSCM\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n    <tr>\n      <td colspan=\"2\" class=\"borderless\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"project_add\" onclick=\"javascript:document.getElementById('reqAction').value='conAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"cancel\" onclick=\"javascript:document.projects.submit();\">\n      </td>\n    </tr>\n  </table>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n";
		expected2 = "";
		assertThat(builder.toString(), equalTo(expected));
		assertThat(builder2.toString(),equalTo(expected2));
		
		String ACT_REQ_REM_PROJECT = Whitebox.<String>getInternalState(ProjectsView.class,"ACT_REQ_REM_PROJECT", ProjectsView.class);
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "createFrom",builder,builder2,storedProject,ACT_REQ_REM_PROJECT,0);
		expected = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tr>\n    <td colspan=\"6\" class=\"noattr\">\nno_projects</td>\n  </tr>\n  <tr class=\"subhead\">\n    <td>View</td><td colspan=\"6\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n      <select name=\"reqUpd\" id=\"reqUpd\" >\n      <optgroup label=\"Import Stage\">      </optgroup>      <optgroup label=\"Parse Stage\">      </optgroup>      <optgroup label=\"Inference Stage\">      </optgroup>      <optgroup label=\"Default Stage\">      </optgroup>      </select>      <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n    </td>\n  <td colspan=\"2\" align=\"right\">\n  <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n</tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>Project information</legend>\n    <table class=\"borderless\">\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n      <tr>\n        <td colspan=\"2\" class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_back\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>\n  </fieldset>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table class=\"borderless\" width='100%'>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectName\" name=\"projectName\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectHomepage\" name=\"projectHomepage\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectContact\" name=\"projectContact\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectBL\" name=\"projectBL\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectML\" name=\"projectML\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectSCM\" name=\"projectSCM\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n    <tr>\n      <td colspan=\"2\" class=\"borderless\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"project_add\" onclick=\"javascript:document.getElementById('reqAction').value='conAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"cancel\" onclick=\"javascript:document.projects.submit();\">\n      </td>\n    </tr>\n  </table>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>l0059: null</legend>\n    <table class=\"borderless\">      <tr>\n        <td class=\"borderless\"><b>delete_project</b></td>\n      </tr>\n      <tr>\n        <td class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0006\" onclick=\"javascript:document.getElementById('reqAction').value='conRemProject';document.projects.submit();\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0004\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>    </fieldset>\n    <input type='hidden' id='reqAction' name='reqAction' value=''>\n    <input type='hidden' id='projectId' name='projectId' value='0'>\n    <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n  </form>\n";
		expected2 = "";
		assertThat(builder.toString(), equalTo(expected));
		assertThat(builder2.toString(),equalTo(expected2));
		
		Set<StoredProject> storedProjects = new HashSet<StoredProject>();
		storedProjects.add(storedProject);
		when(clusterNode.getProjects()).thenReturn(storedProjects);
		ProjectVersion projectVersion = mock(ProjectVersion.class);
		when(ProjectVersion.getLastProjectVersion(storedProject)).thenReturn(projectVersion);
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "createFrom",builder,builder2,storedProject,"action",0);
		expected = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tr>\n    <td colspan=\"6\" class=\"noattr\">\nno_projects</td>\n  </tr>\n  <tr class=\"subhead\">\n    <td>View</td><td colspan=\"6\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n      <select name=\"reqUpd\" id=\"reqUpd\" >\n      <optgroup label=\"Import Stage\">      </optgroup>      <optgroup label=\"Parse Stage\">      </optgroup>      <optgroup label=\"Inference Stage\">      </optgroup>      <optgroup label=\"Default Stage\">      </optgroup>      </select>      <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n    </td>\n  <td colspan=\"2\" align=\"right\">\n  <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n</tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>Project information</legend>\n    <table class=\"borderless\">\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n      <tr>\n        <td colspan=\"2\" class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_back\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>\n  </fieldset>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table class=\"borderless\" width='100%'>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectName\" name=\"projectName\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectHomepage\" name=\"projectHomepage\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectContact\" name=\"projectContact\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectBL\" name=\"projectBL\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectML\" name=\"projectML\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectSCM\" name=\"projectSCM\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n    <tr>\n      <td colspan=\"2\" class=\"borderless\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"project_add\" onclick=\"javascript:document.getElementById('reqAction').value='conAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"cancel\" onclick=\"javascript:document.projects.submit();\">\n      </td>\n    </tr>\n  </table>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>l0059: null</legend>\n    <table class=\"borderless\">      <tr>\n        <td class=\"borderless\"><b>delete_project</b></td>\n      </tr>\n      <tr>\n        <td class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0006\" onclick=\"javascript:document.getElementById('reqAction').value='conRemProject';document.projects.submit();\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0004\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>    </fieldset>\n    <input type='hidden' id='reqAction' name='reqAction' value=''>\n    <input type='hidden' id='projectId' name='projectId' value='0'>\n    <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n  </form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tbody>\n    <tr class=\"selected\" onclick=\"javascript:document.getElementById('projectId').value='';document.projects.submit();\">\n      <td class=\"trans\">0</td>\n      <td class=\"trans\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_info\" onclick=\"javascript:document.getElementById('reqAction').value='conShowProject';document.projects.submit();\">&nbsp;null</td>\n      <td class=\"trans\">0(null)</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">project_not_evaluated</td>\n      <td class=\"trans\">(local)</td>\n    </tr>\n    <tr class=\"subhead\">\n      <td>View</td><td colspan=\"6\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n        <select name=\"reqUpd\" id=\"reqUpd\" >\n        <optgroup label=\"Import Stage\">        </optgroup>        <optgroup label=\"Parse Stage\">        </optgroup>        <optgroup label=\"Inference Stage\">        </optgroup>        <optgroup label=\"Default Stage\">        </optgroup>        </select>        <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n      </td>\n    <td colspan=\"2\" align=\"right\">\n    <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n  </td>\n</tr>\n  </tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n";
		expected2 = "";
		assertThat(builder.toString(), equalTo(expected));
		assertThat(builder2.toString(),equalTo(expected2));
		
		when(storedProject.isEvaluated()).thenReturn(true);
		when(storedProject.getClusternode()).thenReturn(clusterNode);
		Whitebox.<StoredProject>invokeMethod(ProjectsView.class, "createFrom",builder,builder2,storedProject,"action",0);
		expected = "<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tr>\n    <td colspan=\"6\" class=\"noattr\">\nno_projects</td>\n  </tr>\n  <tr class=\"subhead\">\n    <td>View</td><td colspan=\"6\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n      <select name=\"reqUpd\" id=\"reqUpd\" >\n      <optgroup label=\"Import Stage\">      </optgroup>      <optgroup label=\"Parse Stage\">      </optgroup>      <optgroup label=\"Inference Stage\">      </optgroup>      <optgroup label=\"Default Stage\">      </optgroup>      </select>      <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n      <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n    </td>\n  <td colspan=\"2\" align=\"right\">\n  <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n</td>\n</tr>\n</tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>Project information</legend>\n    <table class=\"borderless\">\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n\n      <tr>\n        <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n        <td class=\"borderless\">\n          \n        </td>\n      </tr>\n      <tr>\n        <td colspan=\"2\" class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_back\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>\n  </fieldset>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table class=\"borderless\" width='100%'>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Project name</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectName\" name=\"projectName\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Homepage</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectHomepage\" name=\"projectHomepage\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Contact e-mail</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectContact\" name=\"projectContact\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Bug database</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectBL\" name=\"projectBL\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Mailing list</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectML\" name=\"projectML\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n\n    <tr>\n      <td class=\"borderless\" style=\"width:100px;\"><b>Source code</b></td>\n      <td class=\"borderless\">\n        <input type=\"text\" class=\"form\" id=\"projectSCM\" name=\"projectSCM\" value=\"\" size=\"60\">\n      </td>\n    </tr>\n    <tr>\n      <td colspan=\"2\" class=\"borderless\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"project_add\" onclick=\"javascript:document.getElementById('reqAction').value='conAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"cancel\" onclick=\"javascript:document.projects.submit();\">\n      </td>\n    </tr>\n  </table>\n  <input type='hidden' id='reqAction' name='reqAction' value=''>\n  <input type='hidden' id='projectId' name='projectId' value='0'>\n  <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <fieldset>\n    <legend>l0059: null</legend>\n    <table class=\"borderless\">      <tr>\n        <td class=\"borderless\"><b>delete_project</b></td>\n      </tr>\n      <tr>\n        <td class=\"borderless\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0006\" onclick=\"javascript:document.getElementById('reqAction').value='conRemProject';document.projects.submit();\">\n          <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0004\" onclick=\"javascript:document.projects.submit();\">\n        </td>\n      </tr>\n    </table>    </fieldset>\n    <input type='hidden' id='reqAction' name='reqAction' value=''>\n    <input type='hidden' id='projectId' name='projectId' value='0'>\n    <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n  </form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tbody>\n    <tr class=\"selected\" onclick=\"javascript:document.getElementById('projectId').value='';document.projects.submit();\">\n      <td class=\"trans\">0</td>\n      <td class=\"trans\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_info\" onclick=\"javascript:document.getElementById('reqAction').value='conShowProject';document.projects.submit();\">&nbsp;null</td>\n      <td class=\"trans\">0(null)</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">project_not_evaluated</td>\n      <td class=\"trans\">(local)</td>\n    </tr>\n    <tr class=\"subhead\">\n      <td>View</td><td colspan=\"6\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n        <select name=\"reqUpd\" id=\"reqUpd\" >\n        <optgroup label=\"Import Stage\">        </optgroup>        <optgroup label=\"Parse Stage\">        </optgroup>        <optgroup label=\"Inference Stage\">        </optgroup>        <optgroup label=\"Default Stage\">        </optgroup>        </select>        <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n      </td>\n    <td colspan=\"2\" align=\"right\">\n    <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n  </td>\n</tr>\n  </tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n<form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n  <table>\n    <thead>\n      <tr class=\"head\">\n        <td class='head'  style='width: 10%;'>l0066</td>\n        <td class='head' style='width: 35%;'>l0067</td>\n        <td class='head' style='width: 15%;'>l0068</td>\n        <td class='head' style='width: 15%;'>l0069</td>\n        <td class='head' style='width: 15%;'>l0070</td>\n        <td class='head' style='width: 10%;'>l0071</td>\n        <td class='head' style='width: 10%;'>l0073</td>\n      </tr>\n    </thead>\n  <tbody>\n    <tr class=\"selected\" onclick=\"javascript:document.getElementById('projectId').value='';document.projects.submit();\">\n      <td class=\"trans\">0</td>\n      <td class=\"trans\"><input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"btn_info\" onclick=\"javascript:document.getElementById('reqAction').value='conShowProject';document.projects.submit();\">&nbsp;null</td>\n      <td class=\"trans\">0(null)</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">null</td>\n      <td class=\"trans\">project_is_evaluated</td>\n      <td class=\"trans\">null</td>\n    </tr>\n    <tr class=\"subhead\">\n      <td>View</td><td colspan=\"6\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0008\" onclick=\"javascript:window.location='/projects?projectId=0';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"add_project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"l0059\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\"></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n        <select name=\"reqUpd\" id=\"reqUpd\" >\n        <optgroup label=\"Import Stage\">        </optgroup>        <optgroup label=\"Parse Stage\">        </optgroup>        <optgroup label=\"Inference Stage\">        </optgroup>        <optgroup label=\"Default Stage\">        </optgroup>        </select>        <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\">\n        <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\">\n      </td>\n    <td colspan=\"2\" align=\"right\">\n    <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n  </td>\n</tr>\n  </tbody>\n</table>\n</fieldset>\n<input type='hidden' id='reqAction' name='reqAction' value=''>\n<input type='hidden' id='projectId' name='projectId' value='0'>\n<input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n</form>\n";
		expected2 = "";
		assertThat(builder.toString(), equalTo(expected));
		assertThat(builder2.toString(),equalTo(expected2));

	}

	/**
	 * Test method for {@link eu.sqooss.impl.service.webadmin.ProjectsView#render(javax.servlet.http.HttpServletRequest)}.
	 */
	@Test
	public void testRender() {
		HttpServletRequest req = mock(HttpServletRequest.class);
		String result = ProjectsView.render(req);
		String expected = "\n            <form id=\"projects\" name=\"projects\" method=\"post\" action=\"/projects\">\n              <table>\n                <thead>\n                  <tr class=\"head\">\n                    <td class='head'  style='width: 10%;'>Project Id</td>\n                    <td class='head' style='width: 35%;'>Project Name</td>\n                    <td class='head' style='width: 15%;'>Last Version</td>\n                    <td class='head' style='width: 15%;'>Last Email</td>\n                    <td class='head' style='width: 15%;'>Last Bug</td>\n                    <td class='head' style='width: 10%;'>Evaluated</td>\n                    <td class='head' style='width: 10%;'>Host</td>\n                  </tr>\n                </thead>\n              <tr>\n                <td colspan=\"6\" class=\"noattr\">\nNo projects found.</td>\n              </tr>\n              <tr class=\"subhead\">\n                <td>View</td><td colspan=\"6\">\n                  <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Refresh\" onclick=\"javascript:window.location='/projects';\"></td></tr><tr class=\"subhead\"><td>Manage</td><td colspan='6'>\n                  <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Add project\" onclick=\"javascript:document.getElementById('reqAction').value='reqAddProject';document.projects.submit();\">\n                  <input type=\"button\" class=\"install\" style=\"width: 100px;\" value=\"Delete project\" onclick=\"javascript:document.getElementById('reqAction').value='reqRemProject';document.projects.submit();\" disabled></td></tr><tr class='subhead'><td>Update</td><td colspan='4'>\n                  <input type=\"button\" class=\"install\" value=\"Run Updater\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdate';document.projects.submit();\" disabled>\n                  <input type=\"button\" class=\"install\" value=\"Run All Updaters\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAll';document.projects.submit();\" disabled>\n                </td>\n              <td colspan=\"2\" align=\"right\">\n              <input type=\"button\" class=\"install\" value=\"Update all on ClusterNodeName\" onclick=\"javascript:document.getElementById('reqAction').value='conUpdateAllOnNode';document.projects.submit();\">\n            </td>\n          </tr>\n            </tbody>\n          </table>\n        </fieldset>\n        <input type='hidden' id='reqAction' name='reqAction' value=''>\n        <input type='hidden' id='projectId' name='projectId' value=''>\n        <input type='hidden' id='reqParSyncPlugin' name='reqParSyncPlugin' value=''>\n      </form>\n";
		assertThat(result,equalTo(expected));
		
		
	}

}
