Version < 0.6
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<options>
    <proxy useSystemProxy="false" noProxy="false">
          <host>proxy.host</host>
          <port>1234</port>
          <login>login</login>
          <password>password</password>
    </proxy>
    <storages>		
        <storage class="ru.aristar.jnuget.sources.FilePackageSource" indexed="true" storageName="local.index" refreshInterval="2">
            <properties>
                <property value="c:\inetpub\wwwroot\nuget\Packages\" name="folderName"/>
            </properties>			
			<pushStrategy class="ru.aristar.jnuget.sources.ApiKeyPushStrategy">
				<properties>
					<property name="apiKey" value="API_KEY"/>
				</properties>                
			</pushStrategy>
        </storage>
        <storage class="ru.aristar.jnuget.sources.ProxyPackageSource" indexed="true" storageName="remote.index" refreshInterval="60">
	      <properties>
		<property value="c:\ProxyPackages\" name="folderName"/>
		<property value="https://nuget.org/api/v2/" name="url"/>
	      </properties>
	      <pushStrategy class="ru.aristar.jnuget.sources.SimplePushStrategy">
	            <properties>
		          <property name="allow" value="false"/>
	            </properties>                
	      </pushStrategy>
       </storage>
    </storages>
</options>
```


version >= 0.6

Storages

```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<options>
	<proxy useSystemProxy="false" noProxy="false">
		<host>192.168.1.1</host>
		<port>3128</port>
		<login>guest</login>
		<password>guest</password>
	</proxy>
    <storages>
        <storage class="ru.aristar.jnuget.sources.ClassicPackageSource" indexed="true" canPush="true" canDelete="true" refreshInterval="5">
            <properties>
                <property name="folderName" value="${nuget.home}/Packages/"/>
            </properties>
			<beforeTriggers>
			     <trigger class="ru.aristar.jnuget.sources.push.VersionPatternConstraintTrigger">
			          <properties>
                                       <property name="pattern" value="^\d+\.\d+\.\d*[02468]\.?.*$"/>
                                  </properties>
			     </trigger>
			</beforeTriggers>
			<afterTriggers>
                             <trigger class="ru.aristar.jnuget.sources.push.RemoveOldVersionTrigger">
                                 <properties>
                                       <property name="maxPackageCount" value="10"/>
                                 </properties>
                             </trigger>
                        </afterTriggers>
        </storage>
        <storage class="ru.aristar.jnuget.sources.ProxyPackageSource" indexed="true" refreshInterval="120"  canPush="false" canDelete="false" storageName='mainNugetProxy'>
            <properties>
                <property name="folderName" value="${nuget.home}/Proxy/"/>
                <property name="url" value="https://nuget.org/api/v2/"/>
            </properties>
        </storage>
     </storages>
</options>
```

Users:


```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<users>
	<user name="admin" password="adminadmin" apiKey="AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA">
		<role>jnuget-admin</role>
	</user>
</users>

```