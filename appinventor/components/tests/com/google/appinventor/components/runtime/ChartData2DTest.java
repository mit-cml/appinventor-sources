package com.google.appinventor.components.runtime;

import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.shadows.ShadowAsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;
import org.easymock.EasyMock;
import org.junit.Test;
import org.powermock.api.easymock.PowerMock;
import org.robolectric.android.util.concurrent.RoboExecutorService;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static org.easymock.EasyMock.replay;
import static org.junit.Assert.*;

public class ChartData2DTest extends RobolectricTestBase {
  private Chart chartComponent;
  private ChartData2D data;
  private ChartDataModel model;

  @Override
  public void setUp() {
    super.setUp();
    chartComponent = new Chart(getForm());
    data = new ChartData2D(chartComponent);
    model = data.chartDataModel;

    // The ExecutorService used by the ChartData component has
    // to be changed to a RoboExecutorService in order to
    // make unit tests wait for the tasks to be finished.
    data.setExecutorService(new RoboExecutorService());
  }

  @Test
  public void testSetColor() {
    int argb = 0xFFAABBCC;
    data.Color(argb);
    assertEquals(argb, data.Color());
    assertEquals(argb, model.getDataset().getColor());
  }

  @Test
  public void testSetLabel() {
    String label = "Test Label";
    data.Label(label);
    assertEquals(label, data.Label());
    assertEquals(label, model.getDataset().getLabel());
  }

  @Test
  public void testSetColorsSingleColor() {
    YailList colors = YailList.makeList(
        Collections.singletonList(0xFFAABBCC));

    data.Colors(colors);

    assertEquals(colors, data.Colors());
    assertEquals(1, model.getDataset().getColors().size());
  }

  @Test
  public void testSetColorsMultipleColors() {
    YailList colors = YailList.makeList(
        Arrays.asList(0xFFAABBCC, 0xFFBBCCDD, 0xAABBCCDD)
    );

    data.Colors(colors);

    assertEquals(colors, data.Colors());
    assertEquals(3, model.getDataset().getColors().size());
  }

  @Test
  public void testSetColorsInvalidEntries() {
    YailList colors = YailList.makeList(
        Arrays.asList(0xFFAABBCC, "random-entry", 0xBBCCDDEE,
            "string")
    );

    data.Colors(colors);

    YailList expectedColors = YailList.makeList(
        Arrays.asList(0xFFAABBCC, 0xBBCCDDEE)
    );

    assertEquals(expectedColors, data.Colors());
    assertEquals(2, model.getDataset().getColors().size());
  }

  @Test
  public void testAddEntry() {
    assertEquals(0, model.getDataset().getEntryCount());
    data.AddEntry("3", "2");
    assertEquals(1, model.getDataset().getEntryCount());
  }

  @Test
  public void testRemoveEntry() {
    data.AddEntry("1", "1");
    assertEquals(1, model.getDataset().getEntryCount());

    data.RemoveEntry("1", "1");
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testEntryExistsTrue() {
    data.AddEntry("1", "3");
    assertTrue(data.DoesEntryExist("1", "3"));
  }

  @Test
  public void testEntryExistsFalse() {
    data.AddEntry("4", "2");
    assertFalse(data.DoesEntryExist("4", "1"));
  }

  @Test
  public void testImportFromList() {
    assertEquals(0, model.getDataset().getEntryCount());

    YailList entriesList = YailList.makeList(
        Arrays.asList(
            Arrays.asList("1", "1"),
            Arrays.asList("2", "2"),
            Arrays.asList("4", "1")
        )
    );

    data.ImportFromList(entriesList);

    assertEquals(3, model.getDataset().getEntryCount());
  }

  @Test
  public void testClear() {
    data.AddEntry("1", "3");
    data.AddEntry("2", "1");
    assertEquals(2, model.getDataset().getEntryCount());

    data.Clear();
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testGetAllEntries() {
    data.AddEntry("0", "5");
    data.AddEntry("2", "3");
    data.AddEntry("5", "1");

    YailList entries = data.GetAllEntries();

    assertEquals(3, entries.size());
  }

  @Test
  public void testGetEntriesByXValue() {
    data.AddEntry("1", "2");
    data.AddEntry("5", "4");

    YailList entries = data.GetEntriesWithXValue("1");

    assertEquals(1, entries.size());
  }

  @Test
  public void testGetEntriesByYValue() {
    data.AddEntry("0", "3");
    data.AddEntry("1", "3");
    data.AddEntry("2", "4");

    YailList entries = data.GetEntriesWithYValue("3");

    assertEquals(2, entries.size());
  }

  @Test
  public void testSetPointShape() {
    chartComponent.Type(ComponentConstants.CHART_TYPE_SCATTER);
    model = data.chartDataModel;

    IShapeRenderer renderer = ((ScatterDataSet)model.getDataset()).getShapeRenderer();

    data.PointShape(ComponentConstants.CHART_POINT_STYLE_TRIANGLE);
    assertNotSame(renderer, ((ScatterDataSet)model.getDataset()).getShapeRenderer());
  }

  @Test
  public void testSetLineType() {
    LineDataSet.Mode mode = ((LineDataSet)model.getDataset()).getMode();

    data.LineType(ComponentConstants.CHART_LINE_TYPE_STEPPED);
    assertNotSame(mode, ((LineDataSet)model.getDataset()).getMode());
  }

  @Test
  public void testSetElementsFromPairsNull() {
    data.ElementsFromPairs(null);
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testSetElementsFromPairsEmpty() {
    data.ElementsFromPairs("");
    assertEquals(0, model.getDataset().getEntryCount());
  }

  @Test
  public void testSetElementsFromPairs() {
    data.onBeforeInitialize();
    data.ElementsFromPairs("0,1,1,4,2,5");
    assertEquals(3, model.getDataset().getEntryCount());
  }

  @Test
  public void testImportFromDataFile() {
    // Setup expected parameters & expected return value
    YailList expectedParameters = YailList.makeList(Arrays.asList("X", "Y"));

    YailList columns = YailList.makeList(Arrays.asList(
        YailList.makeList(Arrays.asList("X", "1", "2", "3")),
        YailList.makeList(Arrays.asList("Y", "2", "3", "4"))
    ));

    Future returnValue = getMockFutureObject(columns);


    // Setup mock Data File to return the expected value given the
    // expected parameters
    DataFile dataFile = EasyMock.createMock(DataFile.class);
    EasyMock.expect(dataFile.getDataValue(expectedParameters))
        .andReturn(returnValue);

    // Register the Mock Data File
    replay(dataFile);

    // Execute import
    data.ImportFromDataFile(dataFile, "X", "Y");

    // 3 entries are expected to be imported
    assertEquals(3, model.getDataset().getEntryCount());
  }

  @Test
  public void testImportFromTinyDB() {
    String expectedParameter = "TagKey";
    YailList expectedList = YailList.makeList(
        Arrays.asList(
            YailList.makeList(
                Arrays.asList("1", "2")
            ),
            YailList.makeList(
                Arrays.asList("2", "4")
            ),
            YailList.makeList(
                Arrays.asList("4", "2")
            ),
            YailList.makeList(
                Arrays.asList("5", "2")
            )
        )
    );

    // Setup mock TinyDB to return the expected value given the
    // expected parameter
    TinyDB tinyDB = EasyMock.createMock(TinyDB.class);
    EasyMock.expect(tinyDB.getDataValue(expectedParameter))
        .andReturn(expectedList);
    replay(tinyDB);

    data.ImportFromTinyDB(tinyDB, expectedParameter);

    // 4 entries are expected to be imported
    assertEquals(4, model.getDataset().getEntryCount());
  }

  @Test
  public void testImportFromWeb() {
    // Setup expected parameters & expected return value
    YailList expectedParameters = YailList.makeList(Arrays.asList("A", "B"));

    YailList columns = YailList.makeList(Arrays.asList(
        YailList.makeList(Arrays.asList("A", "1", "7")),
        YailList.makeList(Arrays.asList("B", "3", "1"))
    ));

    Future returnValue = getMockFutureObject(columns);

    // Setup mock Web component to return the expected value given the
    // expected parameters
    Web web = EasyMock.createMock(Web.class);
    EasyMock.expect(web.getDataValue(expectedParameters))
        .andReturn(returnValue);

    // Register the Mock Data File
    replay(web);

    // Execute import
    data.ImportFromWeb(web, "A", "B");

    // 3 entries are expected to be imported
    assertEquals(2, model.getDataset().getEntryCount());
  }

  @Test
  public void testDataFileSource() {
    // Setup expected parameters & expected return value
    YailList expectedParameters = YailList.makeList(Arrays.asList("X", "Y"));

    YailList columns = YailList.makeList(Arrays.asList(
        YailList.makeList(Arrays.asList("X", "0", "1", "2", "3", "4")),
        YailList.makeList(Arrays.asList("Y", "1", "2", "3", "5", "7"))
    ));

    Future returnValue = getMockFutureObject(columns);


    // Setup mock Data File to return the expected value given the
    // expected parameters
    DataFile dataFile = EasyMock.createMock(DataFile.class);
    EasyMock.expect(dataFile.getDataValue(expectedParameters))
        .andReturn(returnValue);

    // Register the Mock Data File
    replay(dataFile);

    // Set Source related properties
    data.Source(dataFile);
    data.DataFileXColumn("X");
    data.DataFileYColumn("Y");

    // Initialize Data component
    data.onBeforeInitialize();

    // 5 entries are expected to be imported
    assertEquals(5, model.getDataset().getEntryCount());
  }

  @Test
  public void testWebSource() {
    // Setup expected parameters & expected return value
    YailList expectedParameters = YailList.makeList(Arrays.asList("A", "B"));

    YailList columns = YailList.makeList(Arrays.asList(
        YailList.makeList(Arrays.asList("A", "1", "2", "5")),
        YailList.makeList(Arrays.asList("B", "3", "1", "-3"))
    ));

    Future returnValue = getMockFutureObject(columns);

    // Setup mock Web component to return the expected value given the
    // expected parameters
    Web web = EasyMock.createMock(Web.class);
    EasyMock.expect(web.getDataValue(expectedParameters))
        .andReturn(returnValue);
    EasyMock.expect(web.getColumns(expectedParameters))
        .andReturn(columns);

    // Expect Add Data Observer method call
    web.addDataObserver(data);

    // Register the Mock Data File
    replay(web);

    // Set Source related properties
    data.WebXColumn("A");
    data.WebYColumn("B");
    data.Source(web);

    // Initialize Data component
    data.onBeforeInitialize();

    // 3 entries are expected to be imported
    assertEquals(3, model.getDataset().getEntryCount());
  }

  @Test
  public void testOnDataSourceValueChange() {
    String expectedParameter = "TagKey";
    YailList expectedList = YailList.makeList(
        Arrays.asList(
            YailList.makeList(
                Arrays.asList("1", "2")
            ),
            YailList.makeList(
                Arrays.asList("2", "4")
            ),
            YailList.makeList(
                Arrays.asList("4", "2")
            ),
            YailList.makeList(
                Arrays.asList("5", "2")
            )
        )
    );

    // Setup mock TinyDB to return the expected value given the
    // expected parameter
    TinyDB tinyDB = EasyMock.createMock(TinyDB.class);
    EasyMock.expect(tinyDB.getDataValue(expectedParameter))
        .andReturn(expectedList);


    // Expect addDataObserver method call
    tinyDB.addDataObserver(data);

    replay(tinyDB);

    // Set the Source related properties to the Data component
    data.Source(tinyDB);
    data.DataSourceValue(expectedParameter);

    // Initialize Data component
    data.onBeforeInitialize();

    // 4 entries are expected to be imported
    assertEquals(4, model.getDataset().getEntryCount());

    // Send an onDataSourceValueChange event originating from the
    // attached TinyDB component with the key value and a new value
    YailList newValues = YailList.makeList(
        Arrays.asList(
            YailList.makeList(
                Arrays.asList("0", "1")
            ),
            YailList.makeList(
                Arrays.asList("1", "3")
            )
        )
    );

    data.onDataSourceValueChange(tinyDB, expectedParameter, newValues);

    // The previous values should be deleted, and the new ones
    // should be added, resulting in 2 values in the Data Series.
    assertEquals(2, model.getDataset().getEntryCount());
  }

  @Test
  public void testOnDataSourceValueChangeWeb() {
    // Setup expected parameters & expected return value
    YailList expectedParameters = YailList.makeList(Arrays.asList("A", "B"));

    YailList columns = YailList.makeList(Arrays.asList(
        YailList.makeList(Arrays.asList("A", "1", "2", "5")),
        YailList.makeList(Arrays.asList("B", "3", "1", "-3"))
    ));

    YailList newColumns = YailList.makeList(Arrays.asList(
        YailList.makeList(Arrays.asList("A", "0")),
        YailList.makeList(Arrays.asList("B", "1"))
    ));

    Future returnValue = getMockFutureObject(columns);

    // Setup mock Web component to return the expected value given the
    // expected parameters
    Web web = EasyMock.createMock(Web.class);
    EasyMock.expect(web.getDataValue(expectedParameters))
        .andReturn(returnValue);

    // Expect Add Data Observer method call
    web.addDataObserver(data);

    EasyMock.expect(web.getColumns(expectedParameters))
        .andReturn(columns);

    // Register the Mock Data File
    replay(web);

    // Set the Source related properties to the Data component
    data.Source(web);
    data.WebXColumn("A");
    data.WebYColumn("B");

    // Initialize Data component
    data.onBeforeInitialize();

    // 4 entries are expected to be imported
    assertEquals(3, model.getDataset().getEntryCount());

    EasyMock.reset(web);
    EasyMock.expect(web.getColumns(expectedParameters))
        .andReturn(newColumns);
    replay(web);

    // Send an onDataSourceValueChange event originating from the
    // attached Web component with a new value
     data.onDataSourceValueChange(web, null, newColumns);

    // The previous values should be deleted, and the new ones
    // should be added, resulting in 2 values in the Data Series.
     assertEquals(1, model.getDataset().getEntryCount());
  }

  @Test
  public void testOnReceiveValue() {
    AccelerometerSensor sensor = EasyMock.createMock(AccelerometerSensor.class);
    sensor.addDataObserver(data);
    replay(sensor);

    String keyValue = "X";
    float value = 3f;

    data.DataSourceValue(keyValue);
    data.Source(sensor);
    data.onBeforeInitialize();

    assertEquals(0, model.getDataset().getEntryCount());

    data.onReceiveValue(sensor, keyValue, value);

    assertEquals(1, model.getDataset().getEntryCount());
  }

  // TODO: The following test is finished, however, due to the CloudDB class being final,
  // TODO: the mocking does not work. In order to allow this test to work, it is required
  // TODO: that both Robolectric and PowerMock would run at the same time. However,
  // TODO: this requires updating and pulling in new dependencies.
  // TODO: See: https://github.com/robolectric/robolectric/wiki/Using-PowerMocks
//  @Test
//  public void testImportFromCloudDB() {
//    String expectedParameter = "CloudKey";
//    YailList expectedList = YailList.makeList(
//        Arrays.asList(
//            YailList.makeList(
//                Arrays.asList("0", "7")
//            ),
//            YailList.makeList(
//                Arrays.asList("1", "9")
//            ),
//            YailList.makeList(
//                Arrays.asList("3", "0")
//            )
//        )
//    );
//
//    Future returnValue = getMockFutureObject(expectedList);
//
//    // Setup mock CloudDB to return the expected value given the
//    // expected parameter
//    CloudDB cloudDB = EasyMock.createMock(CloudDB.class);
//    EasyMock.expect(cloudDB.getDataValue(expectedParameter))
//        .andReturn(returnValue);
//    replay(cloudDB);
//
//    data.ImportFromCloudDB(cloudDB, expectedParameter);
//
//    // 3 entries are expected to be imported
//    assertEquals(3, model.getDataset().getEntryCount());
//  }

  private Future getMockFutureObject(Object returnValue) {
    Future futureObject = EasyMock.createMock(Future.class);

    try {
      EasyMock.expect(futureObject.get()).andReturn(returnValue);
    } catch (ExecutionException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    replay(futureObject);

    return futureObject;
  }
}