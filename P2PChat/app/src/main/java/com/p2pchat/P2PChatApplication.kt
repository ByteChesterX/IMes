package com.p2pchat

import android.app.Application
import android.content.Context
import com.p2pchat.data.local.LocalDataSource
import com.p2pchat.network.P2PNetworkManager

class P2PChatApplication : Application() {
    
    lateinit var networkManager: P2PNetworkManager
        private set
    
    lateinit var localDataSource: LocalDataSource
        private set
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        networkManager = P2PNetworkManager(applicationContext)
        localDataSource = LocalDataSource(applicationContext)
    }
    
    companion object {
        lateinit var instance: P2PChatApplication
            private set
            
        val context: Context
            get() = instance.applicationContext
    }
}
