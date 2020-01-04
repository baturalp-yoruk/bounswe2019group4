import * as React from 'react'
import {Popup, Button, Grid, Input, List, Segment} from 'semantic-ui-react';
import annotationFactory from '../../factories/annotationFactory';
import {colorAccent, colorBG, colorLightBG, colorLightgrey} from "../../utils/constants/Colors";
import history from "../../_core/history";
import {loadState} from "../../_core/localStorage";
import authFactory from "../../factories/authFactory";


class ImageMark extends React.Component {
    constructor(props) {
        super(props);
        this.state= {annotationText: "",loading:false,edittext:""}
    }

    onChange(e, data) {
        this.setState({annotationText: data.value});
    }

    async submit() {
        this.setState({loading:true})
        const {annotationText } = this.state;
        let a=await annotationFactory.addAnnotation({
            "@context": "http://www.w3.org/ns/anno.jsonld",
            articleId: this.props.articleId,
            "body": {
                annotationText,
                "articleId": this.props.articleId,
                "finishIndex": this.props.end,
                "h": 0,
                "startIndex": this.props.start,
                "type": "Text",
                "userId": loadState().user._id,
                "w": 0,
                "x": 0,
                "y": 0,
                "username":loadState().user.name+" "+loadState().user.surname
            },
            "target": "http://www.example.com/index.htm",
            "type": "Annotation"
        }).then(()=>{
            this.props.getAnnos().then(()=>{ this.scrollBottom()});
            this.setState({annotationText:"",loading:false})

        })

        this.props.annotate(this.state.annotationText);
    }

    scrollBottom=()=>{

        this.cgroup=document.getElementById("annGroup");
        this.cgroup.scrollTop=this.cgroup.scrollHeight;
    };

    deleteAnno=async (item)=>{
        this.setState({loading:true})
        await annotationFactory.delete({"E-Tag":item.etag}).then(()=>{
            this.props.getAnnos();
            this.setState({loading:false})
        })
    };
    editAnno=async(item)=>{
        this.setState({loading:true})
        let a={
            _id:item.id,
            id:"https://anno.arkenstone.ml/annotations/"+item.id,
            "body": {
                annotationText:this.state.annotationText,
                "articleId": this.props.articleId,
                "finishIndex": this.props.end,
                "h": 0,
                "startIndex": this.props.start,
                "type": "Text",
                "userId": loadState().user._id,
                "w": 0,
                "x": 0,
                "y": 0,
                "username":loadState().user.name+" "+loadState().user.surname
            },
            created:item.date,
            modified:item.modifDate,
            "target": "http://www.example.com/index.htm",
            "type": "Annotation"
        }
        await annotationFactory.modifyAnnotation(a).then(()=>{
            this.props.getAnnos();
            this.setState({annotationText:"",loading:false})
        });
    }

    render() {
        const props = this.props;
        const {annotationText} = this.state;

        return (
            /*
            <mark
                style={{backgroundColor: props.color || '#84d2ff', padding: '0 4px'}}
                data-start={props.start}
                data-end={props.end}
                onClick={() => props.onClick({start: props.start, end: props.end})}
            >
                {props.content}
                {props.tag && (
                    <span style={{fontSize: '0.7em', fontWeight: 500, marginLeft: 6}}>{props.tag}</span>
                )}
            </mark>
            */
            <Popup on='click' pinned trigger={<span style={{textDecoration: "underline",backgroundColor:colorAccent,color:"white"}}

                                                    data-start={props.start}
                                                    data-end={props.end}
                //onClick={() => props.onClick({start: props.start, end: props.end})}
            >{props.content}</span>}
                   flowing hoverable>
                {props.tag ?
                    (props.tag) :
                    (<div>
                            <Segment id={"annGroup"} loading={this.state.loading} textAlign="left" style={{display:"flex",flexDirection:"column",alignItems:"center",borderWidth:2,borderRadius:10,borderColor:"lightgrey",color:"lightgrey",maxHeight:200,overflowY:"auto"}}>
                                <List divided relaxed>
                                    {
                                        this.props.annotations.map(item=>{

                                            if(item.start===this.props.start&&item.end===this.props.end){
                                                return (
                                                    <List.Item>
                                                        <List.Icon name={"comment"} />
                                                        <List.Content>
                                                            <List.Header style={{overflow:"hidden",color:"grey"}}>
                                                                {item.annotext}
                                                            </List.Header>
                                                            <List.Description style={{fontSize:12}}>
                                                                <span style={{cursor:"pointer",color:"blue"}} onClick={()=>history.push("/profile/"+item.userid)}>{item.author}</span> in {item.date}
                                                            </List.Description>
                                                            {
                                                                authFactory.isUserLoggedIn()&&item.userid===loadState().user._id&&(
                                                                    <div>
                                                                        <Button size={"mini"} color={"red"} onClick={()=>this.deleteAnno(item)}>X</Button>
                                                                        <Button size={"mini"} color={"yellow"} onClick={()=>this.editAnno(item)}>edit</Button>
                                                                    </div>
                                                                )
                                                            }
                                                        </List.Content>
                                                    </List.Item>)
                                            }
                                        })
                                    }
                                </List>



                            </Segment>
                            <Input
                                value={annotationText}
                                onChange={this.onChange.bind(this)}
                                placeholder="AnnotationText" action={<Button  disabled={!annotationText} onClick={this.submit.bind(this)} content="Annotate" />}/>
                        </div>
                    )
                }
            </Popup>
        )
    }
}

export default ImageMark