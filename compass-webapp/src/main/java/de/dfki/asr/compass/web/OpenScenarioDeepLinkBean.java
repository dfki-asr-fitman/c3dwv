/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.dfki.asr.compass.fitman.web;

import de.dfki.asr.compass.model.Project;
import de.dfki.asr.compass.model.Scenario;
import de.dfki.asr.compass.web.backingbeans.CompassBean;
import de.dfki.asr.compass.web.backingbeans.project.OpenScenarioBean;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author jan
 */
@Named
@SessionScoped
public class OpenScenarioDeepLinkBean extends CompassBean implements Serializable {
    
    	private static final long serialVersionUID = -1L;

    	@Inject
	private OpenScenarioBean openScenarioBean;
        
        public Project getProjectById(Long id) {
            for (Project project : openScenarioBean.getProjectList())
                if (project.getId() == id)
                    return project;
            return null;
        }
        
        public Scenario getScenarioForProjectById(Project p, Long id) {
            for (Scenario scenario: openScenarioBean.getScenarioList())
                if (scenario.getProject() == p && scenario.getId() == id)
                    return scenario;
            return null;
        }
        
        public void deepLink() {          
            Long projectId = Long.parseLong(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("project"));
            Long scenarioId = Long.parseLong(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("scenario"));
            
            Project p = getProjectById(projectId);
            if (p == null) {           
                FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, getRedirectURL("/index.xhtml"));
                return;
            }
            openScenarioBean.setSelectedProject(p);
            
            Scenario s = getScenarioForProjectById(p, scenarioId);
            if (s == null) {           
                FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, getRedirectURL("/index.xhtml"));
                return;
            }
            openScenarioBean.setSelectedScenario(s);
            
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler().handleNavigation(FacesContext.getCurrentInstance(), null, getRedirectURL("/editor/editor.xhtml"));
        }

}
