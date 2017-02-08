package jobApplicationApp.dao;

import jobApplicationApp.dao.repository.*;
import jobApplicationApp.dto.form.ApplicationForm;
import jobApplicationApp.dto.form.ApplicationParamForm;
import jobApplicationApp.dto.form.CompetenceForm;
import jobApplicationApp.entity.*;
import jobApplicationApp.exception.NotValidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;

@Repository
@Qualifier("mysql")
public class MysqlApplicationDao implements ApplicationDao{

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private PersonRepository personRepository;
    @Autowired private CompetenceProfileRepository competenceProfileRepository;
    @Autowired private AvailableRepository availableRepository;
    @Autowired private CompetenceRepository competenceRepository;

    @PersistenceContext
    private EntityManager em;

    @Override
    public ApplicationEntity getApplicationById(int id) {
        return applicationRepository.findOne(id);
    }

    @Override
    public void changeApplicationStatus(int applicationId, String status) {
        ApplicationEntity a = getApplicationById(applicationId);
        ApplicationStatusEntity newStatus = statusRepository.findByName(status);
        a.changeStatus(newStatus);
        applicationRepository.save(a);
    }

    @Override
    public void insertApplication(ApplicationForm application) throws NotValidArgumentException {
        ApplicationStatusEntity status = statusRepository.findByName("PENDING");
        Date registrationDate = new Date();
        PersonEntity person = personRepository.findOne(application.getPersonId());

        AvailabilityEntity availability;
        Date from = application.getAvailableForWork().getFromDate();
        Date to = application.getAvailableForWork().getToDate();
        List<AvailabilityEntity> a = availableRepository.findByFromDateAndToDate(from,to);

        if(a.size() == 0){
            availability = new AvailabilityEntity(from,to);
            availableRepository.save(availability);
        }else {
            availability = a.get(0);
        }

        ApplicationEntity newApplication = new ApplicationEntity(person,registrationDate,status,availability);
        newApplication = applicationRepository.save(newApplication);
        ArrayList<CompetenceProfileEntity> competenceProfileEntities = new ArrayList<>();
            for (CompetenceForm competenceProfileEntity : application.getCompetenceProfile()) {
                CompetenceEntity competence = competenceRepository.findByName(competenceProfileEntity.getName());
                if(competence == null){
                    throw new NotValidArgumentException("Not valid name on competence");
                }
                CompetenceProfileEntity c = new CompetenceProfileEntity(newApplication,competence, competenceProfileEntity.getYearsOfExperience());
                competenceProfileEntities.add(c);
        }
        competenceProfileEntities.forEach((c)->competenceProfileRepository.save(c));
    }

    @Override
    public Collection<ApplicationEntity> getXApplicationsFrom(int startId, int numberOfApplication) {
        return applicationRepository.getXApplicationsFrom(startId,numberOfApplication);
    }

    @Override
    public Collection<ApplicationEntity> getApplicationByParam(ApplicationParamForm param) {
        Collection<ApplicationEntity> resultListOfApplication= new ArrayList<>();
        Collection<ApplicationEntity> sqlResultOfApplication;

        if(param.getAvailableForWork() != null) {
            if (param.getAvailableForWork().getFromDate() != null) {
                sqlResultOfApplication = applicationRepository.getApplicationsThatCanWorkFrom(param.getAvailableForWork().getFromDate());
                if (sqlResultOfApplication.size() == 0) {return new ArrayList<>();}
                if(resultListOfApplication.size() == 0){
                    resultListOfApplication = sqlResultOfApplication;
                }else {
                    resultListOfApplication = getListOfIntersectionBetweenApplicationList(sqlResultOfApplication, resultListOfApplication);
                }
            }
            if (param.getAvailableForWork().getToDate() != null) {
                sqlResultOfApplication = applicationRepository.getApplicationsThatCanWorkTo(param.getAvailableForWork().getToDate());
                if (sqlResultOfApplication.size() == 0) {return new ArrayList<>();}
                if(resultListOfApplication.size() == 0){
                    resultListOfApplication = sqlResultOfApplication;
                }else {
                    resultListOfApplication = getListOfIntersectionBetweenApplicationList(sqlResultOfApplication, resultListOfApplication);
                }
            }
        }
        if(param.getCompetenceProfile() != null){
            StringBuilder sqlCondition = new StringBuilder("SELECT a FROM ApplicationEntity a WHERE 1=1 ");
            for(CompetenceForm cf : param.getCompetenceProfile()){
                CompetenceEntity competenceEntity = competenceRepository.findByName(cf.getName());
                if(competenceEntity== null){
                    throw new NotValidArgumentException("Not existing competence");
                }else {
                    sqlCondition.append("AND " +competenceEntity.getId()+ " IN (SELECT co.competence.id FROM CompetenceProfileEntity co WHERE co.application.id=a.id)");
                }
                log.info(sqlCondition.toString());
                Query query = em.createQuery(sqlCondition.toString());
                sqlResultOfApplication =  query.getResultList();
                if (sqlResultOfApplication.size() == 0) {return new ArrayList<>();}
                if(resultListOfApplication.size() == 0){
                    resultListOfApplication = sqlResultOfApplication;
                }else {
                    resultListOfApplication = getListOfIntersectionBetweenApplicationList(sqlResultOfApplication, resultListOfApplication);
                }
            }
        }
        if(param.getName() != null){
            sqlResultOfApplication = applicationRepository.getApplicationsByPersonName(param.getName());
            if (sqlResultOfApplication.size() == 0) {return new ArrayList<>();}
            if(resultListOfApplication.size() == 0){
                resultListOfApplication = sqlResultOfApplication;
            }else {
                resultListOfApplication = getListOfIntersectionBetweenApplicationList(sqlResultOfApplication, resultListOfApplication);
            }
        }
        return resultListOfApplication;
    }

    private Collection<ApplicationEntity> getListOfIntersectionBetweenApplicationList(Collection<ApplicationEntity> map1, Collection<ApplicationEntity> map2){
        Collection<ApplicationEntity> applicationListResult = new ArrayList<>();
        map1.forEach((k)->{
            if(map2.contains(k)){
                applicationListResult.add(k);
            }
        });

        return applicationListResult;
    }
}

