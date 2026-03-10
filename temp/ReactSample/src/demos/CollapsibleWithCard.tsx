/**
 * TEST B: Collapsible + Card
 * The big question: how should these two interact?
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Box from '@mui/material/Box';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Accordion from '@mui/material/Accordion';
import AccordionSummary from '@mui/material/AccordionSummary';
import AccordionDetails from '@mui/material/AccordionDetails';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { SampleChildrenVertical } from './SampleChildren';

export function CollapsibleWithCard() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Test B: Collapsible + Card</Typography>

      {/* Option B1: Collapsible replaces Card entirely */}
      <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
        B1: Collapsible REPLACES Card (collapsible wins)
      </Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h5">Group Label</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* Option B2: Card wraps Accordion */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>
        B2: Card wraps Accordion (Card outside, Accordion inside)
      </Typography>
      <Grid size={{ xs: 12 }}>
        <Card>
          <CardContent>
            <Accordion defaultExpanded elevation={0} disableGutters sx={{ '&:before': { display: 'none' } }}>
              <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={{ px: 0 }}>
                <Typography variant="h5">Group Label</Typography>
              </AccordionSummary>
              <AccordionDetails sx={{ px: 0 }}>
                <Stack spacing={2}>
                  <SampleChildrenVertical />
                </Stack>
              </AccordionDetails>
            </Accordion>
          </CardContent>
        </Card>
      </Grid>

      {/* Option B3: Accordion with Card-like styling (variant="outlined") */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>
        B3: Accordion with Card-like styling (elevation + outlined)
      </Typography>
      <Grid size={{ xs: 12 }}>
        <Accordion defaultExpanded variant="outlined" sx={{ borderRadius: 1 }}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h5">Group Label</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Stack spacing={2}>
              <SampleChildrenVertical />
            </Stack>
          </AccordionDetails>
        </Accordion>
      </Grid>

      {/* Option B4: Card header stays visible, only content collapses */}
      <Typography variant="subtitle2" sx={{ mt: 3, mb: 1 }}>
        B4: Card with collapsible content only (header always visible)
      </Typography>
      <Grid size={{ xs: 12 }}>
        <Card>
          <Accordion defaultExpanded elevation={0} disableGutters sx={{ '&:before': { display: 'none' } }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h5">Group Label</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <CardContent sx={{ pt: 0 }}>
                <Stack spacing={2}>
                  <SampleChildrenVertical />
                </Stack>
              </CardContent>
            </AccordionDetails>
          </Accordion>
        </Card>
      </Grid>
    </Box>
  );
}
