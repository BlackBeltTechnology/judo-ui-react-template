# TabController Element Specification
**Created:** 2025-11-28
**Status:** ✅ Complete  

---

```
}
  );
    </Box>
      ))}
        </TabPanel>
          ))}
            <VisualElementRenderer key={child.id} element={child} data={data} />
          {tab.children.map(child => (
        <TabPanel key={tab.id} value={activeTab} index={index}>
      {visibleTabs.map((tab, index) => (
      
      </Tabs>
        ))}
          />
            disabled={tab.disabled}
            icon={tab.icon && <Icon name={tab.icon.name} />}
            label={tab.label}
            key={tab.id}
          <Tab
        {visibleTabs.map((tab, index) => (
      <Tabs value={activeTab} onChange={(e, v) => setActiveTab(v)}>
    <Box>
  return (
  
  );
    !tab.hiddenBy || !data[tab.hiddenBy]
  const visibleTabs = model.tabs.filter(tab => 
  
  const [activeTab, setActiveTab] = useState(model.defaultTab);
function TabController({ model, data }: TabControllerProps) {

}
  data: any;
  model: TabControllerModel;
interface TabControllerProps {
```typescript

## Component Interface

TabController organizes content into tabs, enabling multi-section forms and views.

## Overview

**Dependencies:** `runtime-model/03-container-model.md`  
**Assigned:** AI Agent  
**Status:** Complete  
**Domain:** Visual Elements / Containers  


