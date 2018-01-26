package com.mk.cryptobox;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Coin
{
    @SerializedName("id") @Expose private String id;
    @SerializedName("name") @Expose private String name;
    @SerializedName("symbol") @Expose private String symbol;
    private String image;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }

    public String getImage()
    {
        return image;
    }

    public void setImage(String image)
    {
        this.image = image;
    }
}
