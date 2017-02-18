package service;

import jobApplicationApp.JobApplicationLauncher;
import jobApplicationApp.dao.MysqlApplicationDao;
import jobApplicationApp.dto.form.ApplicationParamForm;
import jobApplicationApp.dto.form.ApplicationStatusForm;
import jobApplicationApp.entity.*;
import jobApplicationApp.exception.NotValidArgumentException;
import jobApplicationApp.service.JobApplicationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import utils.JobApplicationEntityGenerater;
import utils.JobApplicationFormGenerater;

import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = JobApplicationLauncher.class)
@ActiveProfiles("test")
public class JobApplicationServiceTest {

    private JobApplicationFormGenerater jobApplicationFormGenerater = new JobApplicationFormGenerater();
    private JobApplicationEntityGenerater jobApplicationEntityGenerater = new JobApplicationEntityGenerater();

    @MockBean
    private MysqlApplicationDao mysqlApplicationDao;

    @Autowired
    JobApplicationService jobApplicationService;

    @Test
    public void getAllValidCompetences(){
        Collection<CompetenceEntity> collection = new ArrayList<>();
        collection.add(new CompetenceEntity("super skills"));
        collection.add(new CompetenceEntity("bad skills"));
        given(this.mysqlApplicationDao.getAllValidCompetences("en")).willReturn(collection);
        Collection<CompetenceEntity> returnList = jobApplicationService.getAllValidCompetences("en");
        collection.forEach((v)->{
            assertThat(returnList).contains(v);
        });
    }

    @Test
    public void getAllValidStatus() {
        Collection<ApplicationStatusEntity> collection = new ArrayList<>();
        collection.add(new ApplicationStatusEntity("PENDING"));
        collection.add(new ApplicationStatusEntity("ACCEPTED"));
        given(this.mysqlApplicationDao.getAllValidStatus("en")).willReturn(collection);
        Collection<ApplicationStatusEntity> returnList = jobApplicationService.getAllValidStatus("en");
        collection.forEach((v)->{
            assertThat(returnList).contains(v);
        });
    }

    @Test
    public void getApplicationById() {
        ApplicationEntity  applicationEntity  = jobApplicationEntityGenerater.generateApplicationEntity();

        given(this.mysqlApplicationDao.getApplicationById(1,"en")).willReturn(applicationEntity);
        ApplicationEntity returnApplication = jobApplicationService.getApplicationById(1,"en");
        assertEquals(returnApplication.getStatus().getName(), "PENDING");
    }

    @Test
    public void getApplicationByBadId() {
        ApplicationEntity  applicationEntity  = jobApplicationEntityGenerater.generateApplicationEntity();
        given(this.mysqlApplicationDao.getApplicationById(-1,"en")).willReturn(applicationEntity);
        try{
            ApplicationEntity returnApplication = jobApplicationService.getApplicationById(-2,"en");
            fail("Not allowed id was accepted");
        }catch (NotValidArgumentException e){}
    }


    @Test
    public void getNoneExistingApplicationById() {
        given(this.mysqlApplicationDao.getApplicationById(1,"en")).willReturn(new ApplicationEntity());
        ApplicationEntity returnApplication = jobApplicationService.getApplicationById(1,"en");
        assertThat(returnApplication.getAvailableForWork()).isEqualTo(null);
        assertThat(returnApplication.getCompetenceProfile()).isEqualTo(null);
        assertThat(returnApplication.getDateOfRegistration()).isEqualTo(null);
    }

    @Test
    public void getApplicationsByParam() {
        ApplicationParamForm applicationParamForm = new ApplicationParamForm("Sven",null,null);
        Collection<ApplicationEntity> collection = new ArrayList<>();
        collection.add(new ApplicationEntity());//1
        collection.add(new ApplicationEntity());//2
        collection.add(new ApplicationEntity());//3
        collection.add(new ApplicationEntity());//4
        given(this.mysqlApplicationDao.getApplicationByParam(applicationParamForm, "en")).willReturn(collection);
        Collection<ApplicationEntity> returnApplicationEntities = jobApplicationService.getApplicationsByParam(applicationParamForm, "en");
        assertEquals(returnApplicationEntities.size(),4);
    }

    @Test
    public void getApplicationsPage(){
        Collection<ApplicationEntity> collection = new ArrayList<>();
        for(int i=0; i < 10; i++) {
            collection.add(new ApplicationEntity());
        }
        given(this.mysqlApplicationDao.getXApplicationsFrom(0,10, "en")).willReturn(collection);
        Collection<ApplicationEntity> returnApplicationEntities  = jobApplicationService.getApplicationsPage(10,0, "en");
        assertEquals(returnApplicationEntities.size(),10);
    }

    @Test
    public void getApplicationsPageWithBadSize(){
        Collection<ApplicationEntity> collection = new ArrayList<>();
        for(int i=0; i < 10; i++) {
            collection.add(new ApplicationEntity());
        }
        given(this.mysqlApplicationDao.getXApplicationsFrom(0,-5, "en")).willReturn(collection);
        try {
            Collection<ApplicationEntity> returnApplicationEntities  = jobApplicationService.getApplicationsPage(-5,0, "en");
            fail("Not valid page size was accepted");
        }catch (NotValidArgumentException e){
        }
    }

    @Test
    public void getApplicationsPageByBadStartId(){
        Collection<ApplicationEntity> collection = new ArrayList<>();
        for(int i=0; i < 10; i++) {
            collection.add(new ApplicationEntity());
        }
        given(this.mysqlApplicationDao.getXApplicationsFrom(-5,10, "en")).willReturn(collection);
        try {
            Collection<ApplicationEntity> returnApplicationEntities  = jobApplicationService.getApplicationsPage(10,-5, "en");
            fail("Not valid page size was accepted");
        }catch (NotValidArgumentException e){
        }
    }

    @Test
    public void changeStatusOnApplicationByIdTest(){
        ApplicationStatusForm applicationStatusForm = jobApplicationFormGenerater.getApplicationStatusForm();
        try {
            jobApplicationService.changeStatusOnApplicationById(5, applicationStatusForm);
        }catch (Exception e){
            fail("could not change on fake application");
        }
    }

    @Test
    public void registerJobApplicationTest(){
        try {
            jobApplicationService.registerJobApplication(jobApplicationFormGenerater.generateApplicationForm());
        }catch (Exception e){
            fail("could not register new application from service");
        }
    }
}
