package de.uzk.hki.da.cb;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.uzk.hki.da.model.DAFile;
import de.uzk.hki.da.model.Event;
import de.uzk.hki.da.model.Job;
import de.uzk.hki.da.model.Object;
import de.uzk.hki.da.service.MimeTypeDetectionService;
import de.uzk.hki.da.utils.Path;
import de.uzk.hki.da.utils.RelativePath;
import de.uzk.hki.da.utils.TESTHelper;

public class UpdateMetadataActionLIDOTests {
	
	private static final Namespace LIDO_NS = Namespace.getNamespace("http://www.lido-schema.org");
	private static MimeTypeDetectionService mtds;
	private static final Path workAreaRootPathPath = new RelativePath("src/test/resources/cb/UpdateMetadataActionLIDOTests/");
	private static final UpdateMetadataAction action = new UpdateMetadataAction();
	private Event event1;
	private Event event2;
	private Object object;
	
	@BeforeClass
	public static void mockDca() throws IOException {
		mtds = mock(MimeTypeDetectionService.class);
		when(mtds.detectMimeType((DAFile)anyObject())).thenReturn("image/tiff");
	}
	
	@Before
	public void setUp() throws IOException {
		object = TESTHelper.setUpObject("42",workAreaRootPathPath);

		FileUtils.copyFileToDirectory(Path.make(workAreaRootPathPath,"work/src/LIDO-Testexport2014-07-04-FML-Auswahl.xml").toFile(), Path.make(workAreaRootPathPath,"work/TEST/42/data/a/").toFile());
		DAFile f1 = new DAFile(object.getLatestPackage(),"a","LIDO-Testexport2014-07-04-FML-Auswahl.xml");
		object.getLatestPackage().getFiles().add(f1);
		
		event1 = new Event();
		event1.setSource_file(new DAFile(object.getLatestPackage(),"a","LVR_DFG-Alltagskultur_0000050177.tif"));
		event1.setTarget_file(new DAFile(object.getLatestPackage(),"b","renamed0000050177.tif"));
		event1.setType("CONVERT");
		
		event2 = new Event();
		event2.setSource_file(new DAFile(object.getLatestPackage(),"a","LVR_DFG-Alltagskultur_0000050178.tif"));
		event2.setTarget_file(new DAFile(object.getLatestPackage(),"b","renamed0000050178.tif"));
		event2.setType("CONVERT");
		
		object.getLatestPackage().getEvents().add(event1);
		object.getLatestPackage().getEvents().add(event2);
		
		Job job = new Job(); job.setObject(object); job.setId(1);
		object.setPackage_type("LIDO");
		object.setMetadata_file("LIDO-Testexport2014-07-04-FML-Auswahl.xml");
		
		HashMap<String,String> xpaths = new HashMap<String,String>();
		xpaths.put("LIDO", "//lido:linkResource");
		action.setXpathsToUrls(xpaths);
		HashMap<String, String> nsMap = new HashMap<String,String>();
		nsMap.put("lido", LIDO_NS.getURI());
		action.setNamespaces(nsMap);
		
		action.setAbsUrlPrefix("http://data.danrw.de/file");
		Map<String, String> dcMappings = new HashMap<String,String>();
		dcMappings.put("LIDO", "conf/xslt/dc/lido_to_dc.xsl");
		action.setDcMappings(dcMappings);
		
		action.setMtds(mtds);
		action.setObject(object);
		action.setJob(job);
	}
	
	@After 
	public void tearDown(){
		Path.makeFile(workAreaRootPathPath,"work/TEST/42/data/a/LIDO-Testexport2014-07-04-FML-Auswahl.xml").delete();
		Path.makeFile(workAreaRootPathPath,"work/TEST/42/data/b/LIDO-Testexport2014-07-04-FML-Auswahl.xml").delete();
	}
	
	@Test
	public void test() throws IOException, JDOMException {
		
		action.implementation();
		
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new FileReader(Path.make(workAreaRootPathPath,"work/TEST/42/data/b/LIDO-Testexport2014-07-04-FML-Auswahl.xml").toFile()));
		assertEquals("http://data.danrw.de/file/42/renamed0000050177.tif", getLIDOURL(doc));
		
	}
		
	private String getLIDOURL(Document doc){
		
		return doc.getRootElement()
				.getChild("lido", LIDO_NS)
				.getChild("administrativeMetadata", LIDO_NS)
				.getChild("resourceWrap", LIDO_NS)
				.getChild("resourceSet", LIDO_NS)
				.getChild("resourceRepresentation", LIDO_NS)
				.getChild("linkResource", LIDO_NS)
				.getValue();
	}

}
