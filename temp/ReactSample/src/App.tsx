import { useState } from 'react';
import CssBaseline from '@mui/material/CssBaseline';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';

import { PlainFlex } from './demos/PlainFlex';
import { CardOnly } from './demos/CardOnly';
import { CollapsibleOnly } from './demos/CollapsibleOnly';
import { CollapsibleWithCard } from './demos/CollapsibleWithCard';
import { CollapsibleWithLabel } from './demos/CollapsibleWithLabel';
import { CollapsibleWithIcon } from './demos/CollapsibleWithIcon';
import { CollapsibleWithActionButtons } from './demos/CollapsibleWithActionButtons';
import { CollapsibleHorizontal } from './demos/CollapsibleHorizontal';
import { CollapsibleNested } from './demos/CollapsibleNested';
import { CollapsibleWithSubTheme } from './demos/CollapsibleWithSubTheme';

function TabPanel({ children, value, index }: { children: React.ReactNode; value: number; index: number }) {
  return value === index ? <Box sx={{ pt: 3 }}>{children}</Box> : null;
}

function App() {
  const [tab, setTab] = useState(0);

  return (
    <>
      <CssBaseline />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Typography variant="h3" gutterBottom>
          Flex Collapsible Combinations Test
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          Test all combinations of Flex modes with collapsible to find the best approach.
        </Typography>

        <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="scrollable" scrollButtons="auto">
          <Tab label="Plain Flex (baseline)" />
          <Tab label="Card Only (baseline)" />
          <Tab label="A: Collapsible Only" />
          <Tab label="B: Collapsible + Card" />
          <Tab label="C: Collapsible + Label/Icon" />
          <Tab label="D: Collapsible + Icon Only" />
          <Tab label="E: Collapsible + Action Btns" />
          <Tab label="F: Collapsible Horizontal" />
          <Tab label="G: Nested Collapsible" />
          <Tab label="H: Collapsible + SubTheme" />
        </Tabs>

        <TabPanel value={tab} index={0}><PlainFlex /></TabPanel>
        <TabPanel value={tab} index={1}><CardOnly /></TabPanel>
        <TabPanel value={tab} index={2}><CollapsibleOnly /></TabPanel>
        <TabPanel value={tab} index={3}><CollapsibleWithCard /></TabPanel>
        <TabPanel value={tab} index={4}><CollapsibleWithLabel /></TabPanel>
        <TabPanel value={tab} index={5}><CollapsibleWithIcon /></TabPanel>
        <TabPanel value={tab} index={6}><CollapsibleWithActionButtons /></TabPanel>
        <TabPanel value={tab} index={7}><CollapsibleHorizontal /></TabPanel>
        <TabPanel value={tab} index={8}><CollapsibleNested /></TabPanel>
        <TabPanel value={tab} index={9}><CollapsibleWithSubTheme /></TabPanel>
      </Container>
    </>
  );
}

export default App;
