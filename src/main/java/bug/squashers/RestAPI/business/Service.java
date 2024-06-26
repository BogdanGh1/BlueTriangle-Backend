package bug.squashers.RestAPI.business;

import bug.squashers.RestAPI.infrastructure.ActivityRepository;
import bug.squashers.RestAPI.infrastructure.ChildRepository;
import bug.squashers.RestAPI.infrastructure.CommunityActivityRepository;
import bug.squashers.RestAPI.infrastructure.UserRepository;
import bug.squashers.RestAPI.model.*;
import bug.squashers.RestAPI.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
public class Service {

    private final static Logger log = LogManager.getLogger(Service.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private ChildRepository childRepository;
    @Autowired
    private CommunityActivityRepository communityActivityRepository;

    public List<User> findAllUsers() {
        System.out.println("findAllUsers");
        log.info("Service - findAllUsers");
        return userRepository.findAll();
    }

    public User findUserByUsername(String username) {
        log.info("Service - findByUsername : {}", username);
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<Activity> getActivitiesForUserById(ObjectId userId) {
        log.info("Service - getActivitiesForUserById : {}", userId);
        return activityRepository.findByChild_IdOrAdult_Id(userId, userId);
    }

    public List<Activity> getActivitiesForUserByUsername(String username) {
        log.info("Service - getActivitiesForUserByUsername : {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        ObjectId userId = user.get().getId();
        return activityRepository.findByChild_IdOrAdult_Id(userId, userId);
    }

    public List<Activity> findAll() {
        log.info("Service - findAll");
        return activityRepository.findAll();
    }

    public List<CommunityActivity> findAllCommunityActivities() {
        log.info("Service - findAllCommunityActivities");
        return communityActivityRepository.findAll();
    }

    public Activity saveActivity(Activity activity) {
        log.info("Service - saveActivity : {}", activity);
        User adult = activity.getAdult();
        User updatedUser = updateScore(adult, 1);
        return activityRepository.save(activity);
    }

    public CommunityActivity saveCommunityActivity(CommunityActivity communityActivity) {
        log.info("Service - saveCommunityActivity : {}", communityActivity);
        for (User user : communityActivity.getAdults()) {
            updateScore(user, 1);
        }
        User organizer = communityActivity.getOrganizer();
        updateScore(organizer, 1);
        return communityActivityRepository.save(communityActivity);
    }

    public void saveUser(User user) {
        log.info("Service - saveUser : {}", user);
        userRepository.save(user);
    }

    private User updateScore(User adult, int bonusScore) {
        int newScore = adult.getScore() + bonusScore;
        adult.setScore(newScore);
        return userRepository.save(adult);
    }

    public User createUser(String email, String username, String description, String password, String date, Role role) {
        log.info("Service - createUser : {},{},{},{},{},{}", email, username, description, password, date, role);
        String formattedDate = Utils.getFormattedDate(date);
        return userRepository.insert(new User(email, username, password, description, formattedDate, role));
    }

    public List<Child> findAllChildren() {
        log.info("Service - findAllChildren");
        return childRepository.findAll();
    }

    public List<Child> findAllKnownChildren(String username) {
        log.info("Service - findAllKnownChildren");
        return userRepository.findByUsername(username).get().getKnownChildren();
    }

    public Optional<Child> findChild(String childName) {
        log.info("Service - findChild : {}", childName);
        return childRepository.findByName(childName);
    }

    public Optional<User> findUser(String userName) {
        log.info("Service - findUser : {}", userName);
        return userRepository.findByUsername(userName);
    }

    public Optional<Child> findChildByName(String name) {
        log.info("Service - findChildByName : {}", name);
        return childRepository.findByName(name);
    }

    public List<CommunityActivity> findCommunityActivityByOrganizer(User organizer) {
        return communityActivityRepository.findByOrganizer(organizer);
    }

    public Optional<User> login(String username, String password) {
        log.info("Service - login : {}, {}", username, password);
        Optional<User> user = userRepository.findUserByUsernameAndPassword(username, password);
        return user;
    }

    public Activity findActivityByDescriptionAndDate(String description, String date) {
        log.info("Service - findActivityByDescriptionAndDate : {} {}", description, date);
        return activityRepository.findByDescriptionAndDate(description, date);
    }

    public CommunityActivity findCommunityActivityByDescriptionAndDate(String description, String date) {
        log.info("Service - findActivityByDescriptionAndDate : {} {}", description, date);
        return communityActivityRepository.findByDescriptionAndDate(description, date);
    }

    public void feedbackActivity(Activity activity, int feedback) {
        log.info("Service - feedbackActivity : {} {} {}", activity.getDate(), activity.getDescription(), feedback);
        User user = activity.getAdult();
        updateScore(user, feedback);
    }

    public void feedbackCommunityActivity(CommunityActivity activity, int feedback) {
        log.info("Service - feedbackCommunityActivity : {} {} {}", activity.getDate(), activity.getDescription(), feedback);
        User organizer = activity.getOrganizer();
        updateScore(organizer, feedback);
    }

    public User deleteUser(User user) {
        userRepository.delete(user);
        return user;
    }

    public Activity deleteActivity(Activity activity) {
        activityRepository.delete(activity);
        return activity;
    }

    public CommunityActivity deleteCommunityActivity(CommunityActivity communityActivity) {
        communityActivityRepository.delete(communityActivity);
        return communityActivity;
    }
}