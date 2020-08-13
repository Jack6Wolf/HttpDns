package com.jack.dnscache.score;

import java.util.ArrayList;

import com.jack.dnscache.model.DomainModel;
import com.jack.dnscache.model.IpModel;

/**
 * Created by fenglei on 15/4/21.
 */
public interface IScore {

    public String[] serverIpScore(DomainModel domainModel) ;
    
    public String[] ListToArr( ArrayList<IpModel> list) ; 
}
