
package com.ark.bcp.infr.config;

import com.mryx.missconf.client.common.annotations.DisconfFile;
import com.mryx.missconf.client.common.annotations.DisconfFileItem;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 */
@Service
@Scope("singleton")
@DisconfFile(filename = "config.properties")
public class MissconfConfig {

}
