package com.oligei.ticketgathering.daoimpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oligei.ticketgathering.dao.ActitemDao;
import com.oligei.ticketgathering.entity.mongodb.ActitemMongoDB;
import com.oligei.ticketgathering.entity.mysql.Actitem;
import com.oligei.ticketgathering.repository.ActitemMongoDBRepository;
import com.oligei.ticketgathering.repository.ActitemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class ActitemDaoImpl implements ActitemDao {

    @Autowired
    private ActitemRepository actitemRepository;

    @Autowired
    private ActitemMongoDBRepository actitemMongoDBRepository;

    @Override
    /**
     * @param id the actitemId of actitem
     * @return actitem that actitemId equals id
     * @author feaaaaaa
     * @date 2020.8.17
     * @throws NullPointerException when id is null or mongo data is null
     * @throws JpaObjectRetrievalFailureException when id is invalid
     * @throws EmptyResultDataAccessException when actitem is not found
     */
    public Actitem findOneById(Integer id) {
        Objects.requireNonNull(id, "null id --actitemDaoImpl findOneById");
        Actitem actitem = actitemRepository.getOne(id);
        ActitemMongoDB actitemMongoDB = actitemMongoDBRepository.findByActitemId(id);
        if (actitemMongoDB != null)
            actitem.setPrice(actitemMongoDB.getPrice());
        else
            throw new NullPointerException("null mongoDB data of id" + id.toString() + " --actitemDaoImpl findOneById");
        return actitem;
    }

    @Override
    /**
     * @param id the activityId of activity and actitem
     * @return list of actitem that activityId equals id
     * @author feaaaaaa
     * @date 2020.8.17
     * @throws NullPointerException when id is null or mongo data is null
     * @throws JpaObjectRetrievalFailureException when id is invalid
     * @throws EmptyResultDataAccessException when activity is not found
     */
    public List<Actitem> findAllByActivityId(Integer id) {
        Objects.requireNonNull(id, "null id --actitemDaoImpl findAllByActivityId");
        List<Actitem> actitems = actitemRepository.findAllByActivityId(id);
        for (Actitem actitem : actitems) {
            ActitemMongoDB actitemMongoDB = actitemMongoDBRepository.findByActitemId(actitem.getActitemId());
            if (actitemMongoDB != null)
                actitem.setPrice(actitemMongoDB.getPrice());
            else
                throw new NullPointerException("null mongoDB data of id" + id.toString() + "--actitemDaoImpl findAllByActivityId");
        }
        return actitems;
    }

    @Override
    /**
     * @param actitemId
     * @return void
     * @author
     * @date 2020/8/18
     */
    public void deleteMongoDBByActitemId(Integer actitemId) {
        Objects.requireNonNull(actitemId,"null actitemId --ActitemDaoImpl deleteMongoDBByActitemId");
        actitemMongoDBRepository.deleteByActitemId(actitemId);
    }

    @Override
    /**
     * @param actitemId
     * @param price
     * @return ActitemMongoDB
     * @author
     * @date 2020/8/18
     */
    public ActitemMongoDB insertActitemInMongo(Integer actitemId, List<JSONObject> price) {
        Objects.requireNonNull(actitemId,"null actitemId --ActitemDaoImpl insertActitemInMongo");
        Objects.requireNonNull(price,"null price --ActitemDaoaImpl insertActitemInMongo");
        ActitemMongoDB actitemMongoDB = new ActitemMongoDB(actitemId, price);
        return actitemMongoDBRepository.save(actitemMongoDB);
    }

    @Override
    /**
     * @param activityId
     * @param website
     * @return com.oligei.ticketgathering.entity.mysql.Actitem
     * @Author
     * @date 2020/8/18
     */
    public Actitem add(Integer activityId, String website) {
        Objects.requireNonNull(activityId,"null activityId --ActitemDaoImpl add");
        Objects.requireNonNull(website,"null website --ActitemDaoImpl add");
        return actitemRepository.save(new Actitem(null, activityId, website));
    }

//    @Override
//    /**
//     *  delete Actitem from database
//     * @param actitemId
//     * @return Boolean
//     * @author
//     * @date 2020/8/18
//     */
//    public Boolean deleteActitem(Integer actitemId) {
//        actitemRepository.deleteById(actitemId);
//        actitemMongoDBRepository.deleteByActitemId(actitemId);
//        return true;
//    }

    @Override
    /**
     * @param actitemId, price, amount, showtime
     * @return boolean
     * @author Yang Yicheng
     * @date 2020/8/12
     * @throws ArrayIndexOutOfBoundsException no item found so the index overflows
     * @throws ArithmeticException the repository of actitem is zero
     * @throws NullPointerException invalid actiemId expected
     */
    public boolean modifyRepository(int actitemId, int price, int amount, String showtime) {
        Actitem actitem = findOneById(actitemId);
        if (actitem == null || actitem.getPrice() == null) {
            throw new NullPointerException("invalid actiemId --ActitemDaoImpl modifyRepository");
        }
        List<JSONObject> prices = actitem.getPrice();
//        System.out.println(showtime);
        int i, j, repository = 0;
        for (i = 0; i < prices.size(); i++) {
//            System.out.println(prices.get(i).getString("time"));
            if (Objects.equals(showtime, prices.get(i).getString("time"))) {
                break;
            }
        }
        if (i == prices.size()) {
//            System.out.println(i);
            throw new ArrayIndexOutOfBoundsException("null actitem --ActitemDaoImpl modifyRepository");
        }
        JSONObject tmp = prices.get(i);
        JSONArray tickets = tmp.getJSONArray("class");
        for (j = 0; j < tickets.size(); j++) {
            JSONObject ticket = tickets.getJSONObject(j);
            if (Objects.equals(price, Integer.parseInt(ticket.getString("price")))) {
                repository = Integer.parseInt(ticket.getString("num"));
                if (Objects.equals(0, repository) || repository < amount) {
                    throw new ArithmeticException("repository is zero --ActitemDaoImpl modifyRepository");
                } else {
                    repository = repository + amount;
                    ticket.put("num", repository);
                    tickets.set(j, ticket);
                    break;
                }
            }
        }
        if (j == tickets.size()) {
            throw new ArrayIndexOutOfBoundsException("null actitem --ActitemDaoImpl modifyRepository");
        }
        tmp.put("class", tickets);
        prices.set(i, tmp);

        deleteMongoDBByActitemId(actitemId);
        insertActitemInMongo(actitemId, prices);
        return true;
    }
}
