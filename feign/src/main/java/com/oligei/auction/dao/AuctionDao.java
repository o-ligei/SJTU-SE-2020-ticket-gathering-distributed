package com.oligei.auction.dao;

import com.oligei.auction.entity.Auction;

import java.util.List;

public interface AuctionDao {

    Auction save(Auction auction);

    List<Auction> getAvailableAuctionsForNow();

    Auction findOneById(Integer auctionid);

}