package com.oligei.ticketgathering.daoimpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.oligei.ticketgathering.dao.ActitemDao;
import com.oligei.ticketgathering.entity.mongodb.ActitemMongoDB;
import com.oligei.ticketgathering.entity.mysql.Actitem;
import com.oligei.ticketgathering.repository.ActitemMongoDBRepository;
import com.oligei.ticketgathering.repository.ActitemRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Actitem findOneById(Integer id) {
        Actitem actitem = actitemRepository.getOne(id);
        ActitemMongoDB actitemMongoDB = actitemMongoDBRepository.findByActitemId(id);
        try {
            actitem.setPrice(actitemMongoDB.getPrice());
        }
        catch(NullPointerException e){
            throw new NullPointerException("invalid actiemId expected");
        }
        return actitem;
    }

    @Override
    public List<Actitem> findAllByActivityId(Integer id) {
        List<Actitem> actitems = actitemRepository.findAllByActivityId(id);
        for (int i = 0; i < actitems.size(); ++i) {
            ActitemMongoDB actitemMongoDB = actitemMongoDBRepository.findByActitemId(actitems.get(i).getActitemId());
            if (actitemMongoDB == null) System.out.println(actitems.get(i).getActitemId() + "null");
            else actitems.get(i).setPrice(actitemMongoDB.getPrice());
        }
        return actitems;
    }

    @Override
    public void deleteMongoDBByActitemId(Integer actitemId) {
        actitemMongoDBRepository.deleteByActitemId(actitemId);
    }

    @Override
    public ActitemMongoDB insertActitemInMongo(int actitemId, List<JSONObject> price) {
        ActitemMongoDB actitemMongoDB = new ActitemMongoDB(actitemId, price);
        return actitemMongoDBRepository.save(actitemMongoDB);
    }

    @Override
    public Actitem add(int activityId, String website) {
        return actitemRepository.save(new Actitem(null, activityId, website));
    }

    @Override
    public Boolean deleteActitem(Integer actitemId) {
        actitemRepository.deleteById(actitemId);
        actitemMongoDBRepository.deleteByActitemId(actitemId);
        return true;
    }

    @Override
    public boolean modifyRepository(int actitemId, int price, int amount, String showtime) {
        /**
        *@Description modify data in mongoDB and Mysql
        *@Param [actitemId, price, amount, showtime]
        *@return boolean
        *@Author Yang Yicheng
        *@date 2020/8/12
        *@Throws ArrayIndexOutOfBoundsException no item found so the index overflows
        *@Throws ArithmeticException the repository of actitem is zero
        *@Throws NullPointerException invalid actiemId expected
        */
        Actitem actitem = findOneById(actitemId);
        if(actitem==null||actitem.getPrice()==null){
            throw new NullPointerException("invalid actiemId expected");
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
        if(i==prices.size()){
//            System.out.println(i);
            throw new ArrayIndexOutOfBoundsException("no actitem found");
        }
        JSONObject tmp = prices.get(i);
        JSONArray tickets = tmp.getJSONArray("class");
        for (j = 0; j < tickets.size(); j++) {
            JSONObject ticket = tickets.getJSONObject(j);
            if (Objects.equals(price, Integer.parseInt(ticket.getString("price")))) {
                repository = Integer.parseInt(ticket.getString("num"));
                if (Objects.equals(0, repository) || repository<amount) {
                    throw new ArithmeticException("the repository is zero");
                } else {
                    repository = repository +amount;
                    ticket.put("num", repository);
                    tickets.set(j, ticket);
                    break;
                }
            }
        }
        if(j==tickets.size()){
            throw new ArrayIndexOutOfBoundsException("no actitem found");
        }
        tmp.put("class", tickets);
        prices.set(i, tmp);

        deleteMongoDBByActitemId(actitemId);
        insertActitemInMongo(actitemId, prices);
        return true;
    }
}
